package com.fiskmods.lightsabers.tools.model;

import com.fiskmods.lightsabers.client.model.legacy.LegacyModelBase;
import com.fiskmods.lightsabers.client.model.legacy.LegacyModelRenderer;
import com.fiskmods.lightsabers.client.model.legacy.LegacyModelRenderer.CubeDefinition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MaterialDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.UVPair;
import org.joml.Vector3f;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.stream.Stream;

public final class LegacyModelBlockbenchExporter {
    private static final String MODEL_PACKAGE = "com.fiskmods.lightsabers.client.model";
    private static final float MODEL_UNITS_PER_OFFSET = 16.0F;
    private static final float BLOCKBENCH_MODEL_Y_ORIGIN = 24.0F;
    private static final float RADIANS_TO_DEGREES = 180.0F / (float) Math.PI;
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final Field LAYER_MESH_FIELD = reflectedField(
            LayerDefinition.class,
            "mesh"
    );
    private static final Field LAYER_MATERIAL_FIELD = reflectedField(
            LayerDefinition.class,
            "material"
    );
    private static final Field MATERIAL_WIDTH_FIELD = reflectedField(
            MaterialDefinition.class,
            "xTexSize"
    );
    private static final Field MATERIAL_HEIGHT_FIELD = reflectedField(
            MaterialDefinition.class,
            "yTexSize"
    );
    private static final Field PART_CUBES_FIELD = reflectedField(PartDefinition.class, "cubes");
    private static final Field PART_POSE_FIELD = reflectedField(PartDefinition.class, "partPose");
    private static final Field PART_CHILDREN_FIELD = reflectedField(
            PartDefinition.class,
            "children"
    );
    private static final Field CUBE_ORIGIN_FIELD = reflectedField(
            net.minecraft.client.model.geom.builders.CubeDefinition.class,
            "origin"
    );
    private static final Field CUBE_DIMENSIONS_FIELD = reflectedField(
            net.minecraft.client.model.geom.builders.CubeDefinition.class,
            "dimensions"
    );
    private static final Field CUBE_DEFORMATION_FIELD = reflectedField(
            net.minecraft.client.model.geom.builders.CubeDefinition.class,
            "grow"
    );
    private static final Field CUBE_MIRROR_FIELD = reflectedField(
            net.minecraft.client.model.geom.builders.CubeDefinition.class,
            "mirror"
    );
    private static final Field CUBE_TEXTURE_FIELD = reflectedField(
            net.minecraft.client.model.geom.builders.CubeDefinition.class,
            "texCoord"
    );
    private static final Field DEFORMATION_X_FIELD = reflectedField(CubeDeformation.class, "growX");
    private static final Field DEFORMATION_Y_FIELD = reflectedField(CubeDeformation.class, "growY");
    private static final Field DEFORMATION_Z_FIELD = reflectedField(CubeDeformation.class, "growZ");

    private final Options options;
    private final List<Path> availableTextures;

    private LegacyModelBlockbenchExporter(Options options) throws IOException {
        this.options = options;
        availableTextures = findTextures(options.textureRoot());
    }

    public static void main(String[] args) throws Exception {
        Options options = Options.parse(args);
        LegacyModelBlockbenchExporter exporter = new LegacyModelBlockbenchExporter(options);
        exporter.exportAll();
    }

    private void exportAll() throws Exception {
        List<String> modelClasses = findModelClasses();
        List<String> failures = new ArrayList<>();
        int exported = 0;

        for (String className : modelClasses) {
            try {
                if (exportModel(className)) {
                    exported++;
                }
            } catch (ReflectiveOperationException | RuntimeException | IOException exception) {
                failures.add(className + ": " + exception.getMessage());
            }
        }

        int copiedBlockModels = copyBlockModels(
                options.blockModelRoot(),
                options.outputRoot().resolve("block_json/models/block")
        );
        int copiedBlockTextures = copyBlockAssets(
                options.blockTextureRoot(),
                options.outputRoot().resolve("block_json/textures/block"),
                ".png"
        );
        int exportedCompositeBlocks = exportCompositeBlockModels();
        writeBlockAssetReadme();

        System.out.println("Exported " + exported + " Blockbench model(s) to " + options.outputRoot());
        System.out.println(
                "Copied " + copiedBlockModels + " block model JSON file(s) and "
                        + copiedBlockTextures + " block texture(s)"
        );
        System.out.println("Exported " + exportedCompositeBlocks + " complete block .bbmodel project(s)");
        if (!failures.isEmpty()) {
            throw new IllegalStateException("Failed to export models:\n" + String.join("\n", failures));
        }
    }

    private boolean exportModel(String className) throws ReflectiveOperationException, IOException {
        Class<?> rawClass = Class.forName(className);
        if (Modifier.isAbstract(rawClass.getModifiers())) {
            return false;
        }
        if (LegacyModelBase.class.isAssignableFrom(rawClass)) {
            return exportLegacyModel(className, rawClass);
        }

        Method layerFactory = findLayerFactory(rawClass);
        return layerFactory != null && exportLayerModel(className, rawClass, layerFactory);
    }

    private boolean exportLegacyModel(
            String className,
            Class<?> rawClass
    ) throws ReflectiveOperationException, IOException {
        Constructor<?> constructor = rawClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        LegacyModelBase model = (LegacyModelBase) constructor.newInstance();
        Map<LegacyModelRenderer, String> rendererNames = collectRendererNames(rawClass, model);
        if (rendererNames.isEmpty()) {
            return false;
        }

        Path relativeDirectory = classOutputDirectory(className);
        Path outputDirectory = options.outputRoot().resolve(relativeDirectory);
        Files.createDirectories(outputDirectory);
        String simpleName = rawClass.getSimpleName();
        Path modelFile = outputDirectory.resolve(simpleName + ".bbmodel");
        Path mappingFile = outputDirectory.resolve(simpleName + ".mapping.json");
        List<Path> textures = resolveTextures(simpleName);

        ExportContext context = new ExportContext(
                className,
                simpleName,
                model,
                rendererNames,
                textures,
                modelFile
        );
        JsonObject blockbenchModel = createBlockbenchModel(context);
        JsonObject mapping = createMapping(context);
        writeJson(modelFile, blockbenchModel);
        writeJson(mappingFile, mapping);
        System.out.println("  " + className + " -> " + modelFile);
        return true;
    }

    private boolean exportLayerModel(
            String className,
            Class<?> rawClass,
            Method layerFactory
    ) throws ReflectiveOperationException, IOException {
        layerFactory.setAccessible(true);
        LayerDefinition layer = (LayerDefinition) layerFactory.invoke(null);
        ModernModel model = readModernModel(layer);
        if (model.root().children().isEmpty() && model.root().cubes().isEmpty()) {
            return false;
        }

        Path relativeDirectory = classOutputDirectory(className);
        Path outputDirectory = options.outputRoot().resolve(relativeDirectory);
        Files.createDirectories(outputDirectory);
        String simpleName = rawClass.getSimpleName();
        Path modelFile = outputDirectory.resolve(simpleName + ".bbmodel");
        Path mappingFile = outputDirectory.resolve(simpleName + ".mapping.json");
        List<Path> textures = resolveTextures(simpleName);
        ModernExportContext context = new ModernExportContext(
                className,
                simpleName,
                model,
                textures,
                modelFile
        );

        JsonObject blockbenchModel = createModernBlockbenchModel(context);
        JsonObject mapping = createModernMapping(context);
        writeJson(modelFile, blockbenchModel);
        writeJson(mappingFile, mapping);
        System.out.println("  " + className + " -> " + modelFile);
        return true;
    }

    private Method findLayerFactory(Class<?> modelClass) {
        try {
            Method method = modelClass.getDeclaredMethod("createBodyLayer");
            return Modifier.isStatic(method.getModifiers())
                    && LayerDefinition.class.isAssignableFrom(method.getReturnType())
                    ? method
                    : null;
        } catch (NoSuchMethodException exception) {
            return null;
        }
    }

    private JsonObject createBlockbenchModel(ExportContext context) throws IOException {
        JsonObject root = createBlockbenchRoot(
                context.className(),
                context.simpleName(),
                "legacy_model_renderer_raw",
                context.model().textureWidth,
                context.model().textureHeight
        );

        Map<LegacyModelRenderer, LegacyModelRenderer> parents = collectParents(
                context.rendererNames().keySet()
        );
        List<LegacyModelRenderer> roots = context.rendererNames().keySet().stream()
                .filter(renderer -> !parents.containsKey(renderer))
                .sorted(Comparator.comparing(context.rendererNames()::get))
                .toList();

        JsonArray elements = new JsonArray();
        JsonArray outliner = new JsonArray();
        JsonArray mappingNodes = new JsonArray();
        for (LegacyModelRenderer renderer : roots) {
            outliner.add(createGroup(
                    context,
                    renderer,
                    null,
                    Vector.ZERO,
                    elements,
                    mappingNodes
            ));
        }
        root.add("elements", elements);
        root.add("outliner", outliner);
        root.add("textures", createTextures(
                context.className(),
                context.textures(),
                context.modelFile()
        ));
        context.mappingNodes(mappingNodes);
        return root;
    }

    private JsonObject createBlockbenchRoot(
            String className,
            String simpleName,
            String coordinateSystem,
            int textureWidth,
            int textureHeight
    ) {
        JsonObject root = new JsonObject();
        JsonObject meta = new JsonObject();
        meta.addProperty("format_version", "4.5");
        meta.addProperty("model_format", "modded_entity");
        meta.addProperty("box_uv", true);
        root.add("meta", meta);
        root.addProperty("name", simpleName);
        root.addProperty("model_identifier", simpleName);
        root.addProperty("modded_entity_version", "1.17");
        root.addProperty("modded_entity_flip_y", false);
        root.add("visible_box", array(1.0F, 1.0F, 0.0F));
        root.addProperty("variable_placeholders", "");
        root.add("variable_placeholder_buttons", new JsonArray());
        root.add("display", createDefaultBlockDisplay());
        root.add("animations", new JsonArray());
        root.add("animation_controllers", new JsonObject());

        JsonObject sourceMetadata = new JsonObject();
        sourceMetadata.addProperty("source_class", className);
        sourceMetadata.addProperty("coordinate_system", coordinateSystem);
        sourceMetadata.addProperty("exporter_version", 2);
        JsonObject unhandledFields = new JsonObject();
        unhandledFields.add("advanced_lightsabers", sourceMetadata);
        root.add("unhandled_root_fields", unhandledFields);

        JsonObject resolution = new JsonObject();
        resolution.addProperty("width", textureWidth);
        resolution.addProperty("height", textureHeight);
        root.add("resolution", resolution);
        return root;
    }

    private JsonObject createModernBlockbenchModel(
            ModernExportContext context
    ) throws IOException {
        JsonObject root = createBlockbenchRoot(
                context.className(),
                context.simpleName(),
                "layer_definition_blockbench_y_up",
                context.model().textureWidth(),
                context.model().textureHeight()
        );
        JsonArray elements = new JsonArray();
        JsonArray outliner = new JsonArray();
        JsonArray mappingNodes = new JsonArray();

        if (!context.model().root().cubes().isEmpty()) {
            outliner.add(createModernGroup(
                    context,
                    context.model().root(),
                    null,
                    "root",
                    Vector.ZERO,
                    elements,
                    mappingNodes
            ));
        } else {
            for (ModernPart child : context.model().root().children()) {
                outliner.add(createModernGroup(
                        context,
                        child,
                        null,
                        child.name(),
                        Vector.ZERO,
                        elements,
                        mappingNodes
                ));
            }
        }

        root.add("elements", elements);
        root.add("outliner", outliner);
        root.add("textures", createTextures(
                context.className(),
                context.textures(),
                context.modelFile()
        ));
        context.mappingNodes(mappingNodes);
        return root;
    }

    private JsonObject createModernGroup(
            ModernExportContext context,
            ModernPart part,
            String parentPath,
            String path,
            Vector parentRawOrigin,
            JsonArray elements,
            JsonArray mappingNodes
    ) {
        PartPose pose = part.pose();
        Vector rawOrigin = parentRawOrigin.add(pose.x, pose.y, pose.z);
        Vector blockbenchOrigin = toBlockbenchPoint(rawOrigin);
        String groupUuid = stableUuid(context.className() + ":modern_group:" + path);
        JsonObject group = new JsonObject();
        group.addProperty("name", part.name());
        group.add("origin", blockbenchOrigin.toJson());
        if (pose.xRot != 0.0F || pose.yRot != 0.0F || pose.zRot != 0.0F) {
            group.add("rotation", array(
                    -pose.xRot * RADIANS_TO_DEGREES,
                    pose.yRot * RADIANS_TO_DEGREES,
                    -pose.zRot * RADIANS_TO_DEGREES
            ));
        }
        group.addProperty("uuid", groupUuid);
        group.addProperty("export", true);
        group.addProperty("isOpen", false);
        group.addProperty("locked", false);
        group.addProperty("visibility", true);
        group.addProperty("autouv", 0);

        JsonArray children = new JsonArray();
        JsonArray mappingCubes = new JsonArray();
        for (int index = 0; index < part.cubes().size(); index++) {
            ModernCube cube = part.cubes().get(index);
            String cubeUuid = stableUuid(
                    context.className() + ":modern_cube:" + path + ":" + index
            );
            elements.add(createModernCube(
                    context,
                    part.name(),
                    index,
                    cubeUuid,
                    rawOrigin,
                    cube
            ));
            children.add(cubeUuid);
            mappingCubes.add(createModernCubeMapping(index, cubeUuid, cube));
        }

        for (ModernPart child : part.children()) {
            String childPath = path + "/" + child.name();
            children.add(createModernGroup(
                    context,
                    child,
                    path,
                    childPath,
                    rawOrigin,
                    elements,
                    mappingNodes
            ));
        }
        group.add("children", children);

        JsonObject nodeMapping = new JsonObject();
        nodeMapping.addProperty("part", part.name());
        nodeMapping.addProperty("path", path);
        nodeMapping.addProperty("group_uuid", groupUuid);
        if (parentPath == null) {
            nodeMapping.add("parent_path", JsonNull.INSTANCE);
        } else {
            nodeMapping.addProperty("parent_path", parentPath);
        }
        nodeMapping.add("part_pose_offset", array(pose.x, pose.y, pose.z));
        nodeMapping.add("part_pose_rotation_radians", array(
                pose.xRot,
                pose.yRot,
                pose.zRot
        ));
        nodeMapping.add("cubes", mappingCubes);
        mappingNodes.add(nodeMapping);
        return group;
    }

    private JsonObject createModernCube(
            ModernExportContext context,
            String partName,
            int index,
            String cubeUuid,
            Vector rawOrigin,
            ModernCube cube
    ) {
        boolean uniformDeformation = cube.hasUniformDeformation();
        Vector rawFrom = rawOrigin.add(cube.x(), cube.y(), cube.z());
        Vector rawTo = rawOrigin.add(
                cube.x() + cube.width(),
                cube.y() + cube.height(),
                cube.z() + cube.depth()
        );
        if (!uniformDeformation) {
            rawFrom = rawFrom.add(-cube.growX(), -cube.growY(), -cube.growZ());
            rawTo = rawTo.add(cube.growX(), cube.growY(), cube.growZ());
        }
        Vector from = new Vector(
                rawFrom.x(),
                BLOCKBENCH_MODEL_Y_ORIGIN - rawTo.y(),
                rawFrom.z()
        );
        Vector to = new Vector(
                rawTo.x(),
                BLOCKBENCH_MODEL_Y_ORIGIN - rawFrom.y(),
                rawTo.z()
        );
        Vector blockbenchOrigin = toBlockbenchPoint(rawOrigin);

        JsonObject element = new JsonObject();
        element.addProperty("name", partName + "_cube_" + index);
        element.addProperty("box_uv", true);
        element.addProperty("rescale", false);
        element.addProperty("locked", false);
        element.add("from", from.toJson());
        element.add("to", to.toJson());
        element.addProperty("autouv", 0);
        element.addProperty("color", Math.floorMod(partName.hashCode(), 8));
        element.add("origin", blockbenchOrigin.toJson());
        element.add("uv_offset", array(cube.textureOffsetX(), cube.textureOffsetY()));
        if (uniformDeformation && cube.growX() != 0.0F) {
            element.addProperty("inflate", cube.growX());
        }
        if (cube.mirror()) {
            element.addProperty("mirror_uv", true);
        }
        element.add("faces", createFaces(
                !context.textures().isEmpty(),
                cube.textureOffsetX(),
                cube.textureOffsetY(),
                cube.width(),
                cube.height(),
                cube.depth()
        ));
        element.addProperty("type", "cube");
        element.addProperty("uuid", cubeUuid);
        return element;
    }

    private JsonObject createModernCubeMapping(
            int index,
            String cubeUuid,
            ModernCube cube
    ) {
        JsonObject mapping = new JsonObject();
        mapping.addProperty("index", index);
        mapping.addProperty("cube_uuid", cubeUuid);
        mapping.addProperty("texture_offset_x", cube.textureOffsetX());
        mapping.addProperty("texture_offset_y", cube.textureOffsetY());
        mapping.add("position", array(cube.x(), cube.y(), cube.z()));
        mapping.add("size", array(cube.width(), cube.height(), cube.depth()));
        mapping.add("deformation", array(cube.growX(), cube.growY(), cube.growZ()));
        mapping.addProperty("mirror", cube.mirror());
        return mapping;
    }

    private JsonObject createGroup(
            ExportContext context,
            LegacyModelRenderer renderer,
            LegacyModelRenderer parent,
            Vector parentOrigin,
            JsonArray elements,
            JsonArray mappingNodes
    ) {
        String rendererName = context.rendererNames().get(renderer);
        Vector localOrigin = new Vector(
                renderer.rotationPointX + renderer.offsetX * MODEL_UNITS_PER_OFFSET,
                renderer.rotationPointY + renderer.offsetY * MODEL_UNITS_PER_OFFSET,
                renderer.rotationPointZ + renderer.offsetZ * MODEL_UNITS_PER_OFFSET
        );
        Vector origin = parentOrigin.add(localOrigin);
        String groupUuid = stableUuid(context.className() + ":group:" + rendererName);

        JsonObject group = new JsonObject();
        group.addProperty("name", rendererName);
        group.add("origin", origin.toJson());
        if (renderer.rotateAngleX != 0.0F
                || renderer.rotateAngleY != 0.0F
                || renderer.rotateAngleZ != 0.0F) {
            group.add("rotation", array(
                    renderer.rotateAngleX * RADIANS_TO_DEGREES,
                    renderer.rotateAngleY * RADIANS_TO_DEGREES,
                    renderer.rotateAngleZ * RADIANS_TO_DEGREES
            ));
        }
        group.addProperty("uuid", groupUuid);
        group.addProperty("export", true);
        group.addProperty("isOpen", false);
        group.addProperty("locked", false);
        group.addProperty("visibility", renderer.showModel && !renderer.isHidden);
        group.addProperty("autouv", 0);

        JsonArray children = new JsonArray();
        JsonArray mappingCubes = new JsonArray();
        List<CubeDefinition> cubes = renderer.getCubeDefinitions();
        for (int index = 0; index < cubes.size(); index++) {
            CubeDefinition cube = cubes.get(index);
            String cubeUuid = stableUuid(
                    context.className() + ":cube:" + rendererName + ":" + index
            );
            elements.add(createCube(
                    context,
                    rendererName,
                    index,
                    cubeUuid,
                    origin,
                    cube
            ));
            children.add(cubeUuid);
            mappingCubes.add(createCubeMapping(index, cubeUuid, cube));
        }

        List<LegacyModelRenderer> childRenderers = renderer.getChildren().stream()
                .sorted(Comparator.comparing(context.rendererNames()::get))
                .toList();
        for (LegacyModelRenderer child : childRenderers) {
            children.add(createGroup(
                    context,
                    child,
                    renderer,
                    origin,
                    elements,
                    mappingNodes
            ));
        }
        group.add("children", children);

        JsonObject nodeMapping = new JsonObject();
        nodeMapping.addProperty("field", rendererName);
        nodeMapping.addProperty("group_uuid", groupUuid);
        if (parent == null) {
            nodeMapping.add("parent_field", JsonNull.INSTANCE);
        } else {
            nodeMapping.addProperty("parent_field", context.rendererNames().get(parent));
        }
        nodeMapping.add("rotation_point", array(
                renderer.rotationPointX,
                renderer.rotationPointY,
                renderer.rotationPointZ
        ));
        nodeMapping.add("offset", array(renderer.offsetX, renderer.offsetY, renderer.offsetZ));
        nodeMapping.add("rotation_radians", array(
                renderer.rotateAngleX,
                renderer.rotateAngleY,
                renderer.rotateAngleZ
        ));
        nodeMapping.add("cubes", mappingCubes);
        mappingNodes.add(nodeMapping);
        return group;
    }

    private JsonObject createCube(
            ExportContext context,
            String rendererName,
            int index,
            String cubeUuid,
            Vector origin,
            CubeDefinition cube
    ) {
        JsonObject element = new JsonObject();
        element.addProperty("name", rendererName + "_cube_" + index);
        element.addProperty("box_uv", true);
        element.addProperty("rescale", false);
        element.addProperty("locked", false);
        element.add("from", origin.add(cube.x(), cube.y(), cube.z()).toJson());
        element.add("to", origin.add(
                cube.x() + cube.width(),
                cube.y() + cube.height(),
                cube.z() + cube.depth()
        ).toJson());
        element.addProperty("autouv", 0);
        element.addProperty("color", Math.floorMod(rendererName.hashCode(), 8));
        element.add("origin", origin.toJson());
        element.add("uv_offset", array(cube.textureOffsetX(), cube.textureOffsetY()));
        if (cube.inflation() != 0.0F) {
            element.addProperty("inflate", cube.inflation());
        }
        if (cube.mirror()) {
            element.addProperty("mirror_uv", true);
        }
        element.add("faces", createFaces(context, cube));
        element.addProperty("type", "cube");
        element.addProperty("uuid", cubeUuid);
        return element;
    }

    private JsonObject createFaces(ExportContext context, CubeDefinition cube) {
        return createFaces(
                !context.textures().isEmpty(),
                cube.textureOffsetX(),
                cube.textureOffsetY(),
                cube.width(),
                cube.height(),
                cube.depth()
        );
    }

    private JsonObject createFaces(
            boolean hasTexture,
            float u,
            float v,
            float width,
            float height,
            float depth
    ) {
        JsonObject faces = new JsonObject();
        faces.add("north", face(hasTexture, u + depth, v + depth, u + depth + width, v + depth + height));
        faces.add("east", face(hasTexture, u, v + depth, u + depth, v + depth + height));
        faces.add("south", face(
                hasTexture,
                u + depth + width + depth,
                v + depth,
                u + depth + width + depth + width,
                v + depth + height
        ));
        faces.add("west", face(
                hasTexture,
                u + depth + width,
                v + depth,
                u + depth + width + depth,
                v + depth + height
        ));
        faces.add("up", face(hasTexture, u + depth + width, v + depth, u + depth, v));
        faces.add("down", face(
                hasTexture,
                u + depth + width + width,
                v,
                u + depth + width,
                v + depth
        ));
        return faces;
    }

    private JsonObject face(boolean hasTexture, float u1, float v1, float u2, float v2) {
        JsonObject face = new JsonObject();
        face.add("uv", array(u1, v1, u2, v2));
        if (!hasTexture) {
            face.add("texture", JsonNull.INSTANCE);
        } else {
            face.addProperty("texture", 0);
        }
        return face;
    }

    private JsonArray createTextures(
            String className,
            List<Path> modelTextures,
            Path modelFile
    ) throws IOException {
        JsonArray textures = new JsonArray();
        for (int index = 0; index < modelTextures.size(); index++) {
            Path texture = modelTextures.get(index);
            JsonObject entry = new JsonObject();
            entry.addProperty("path", texture.toAbsolutePath().toString());
            entry.addProperty("name", texture.getFileName().toString());
            entry.addProperty("folder", "");
            entry.addProperty("namespace", "lightsabers");
            entry.addProperty("id", Integer.toString(index));
            entry.addProperty("particle", false);
            entry.addProperty("render_mode", "default");
            entry.addProperty("render_sides", "auto");
            entry.addProperty("frame_time", 1);
            entry.addProperty("frame_order_type", "loop");
            entry.addProperty("frame_order", "");
            entry.addProperty("frame_interpolate", false);
            entry.addProperty("visible", index == 0);
            entry.addProperty("mode", "bitmap");
            entry.addProperty("saved", true);
            entry.addProperty("uuid", stableUuid(
                    className + ":texture:" + texture.getFileName()
            ));
            entry.addProperty("relative_path", relativePath(
                    modelFile.getParent(),
                    texture
            ));
            entry.addProperty(
                    "source",
                    "data:image/png;base64," + Base64.getEncoder().encodeToString(
                            Files.readAllBytes(texture)
                    )
            );
            textures.add(entry);
        }
        return textures;
    }

    private JsonObject createMapping(ExportContext context) {
        JsonObject mapping = new JsonObject();
        mapping.addProperty("format_version", 2);
        mapping.addProperty("source_kind", "legacy_model_renderer");
        mapping.addProperty("source_class", context.className());
        mapping.addProperty("bbmodel", context.modelFile().getFileName().toString());
        mapping.addProperty("texture_width", context.model().textureWidth);
        mapping.addProperty("texture_height", context.model().textureHeight);
        JsonArray texturePaths = new JsonArray();
        for (Path texture : context.textures()) {
            texturePaths.add(texture.toAbsolutePath().toString());
        }
        mapping.add("textures", texturePaths);
        mapping.add("nodes", context.mappingNodes());
        return mapping;
    }

    private JsonObject createModernMapping(ModernExportContext context) {
        JsonObject mapping = new JsonObject();
        mapping.addProperty("format_version", 2);
        mapping.addProperty("source_kind", "layer_definition");
        mapping.addProperty("source_class", context.className());
        mapping.addProperty("bbmodel", context.modelFile().getFileName().toString());
        mapping.addProperty("texture_width", context.model().textureWidth());
        mapping.addProperty("texture_height", context.model().textureHeight());
        JsonArray texturePaths = new JsonArray();
        for (Path texture : context.textures()) {
            texturePaths.add(texture.toAbsolutePath().toString());
        }
        mapping.add("textures", texturePaths);
        mapping.add("nodes", context.mappingNodes());
        return mapping;
    }

    private JsonObject createCubeMapping(int index, String cubeUuid, CubeDefinition cube) {
        JsonObject mapping = new JsonObject();
        mapping.addProperty("index", index);
        mapping.addProperty("cube_uuid", cubeUuid);
        mapping.addProperty("texture_offset_x", cube.textureOffsetX());
        mapping.addProperty("texture_offset_y", cube.textureOffsetY());
        mapping.add("position", array(cube.x(), cube.y(), cube.z()));
        mapping.add("size", array(cube.width(), cube.height(), cube.depth()));
        mapping.addProperty("inflation", cube.inflation());
        mapping.addProperty("mirror", cube.mirror());
        return mapping;
    }

    private ModernModel readModernModel(
            LayerDefinition layer
    ) throws IllegalAccessException {
        MeshDefinition mesh = (MeshDefinition) LAYER_MESH_FIELD.get(layer);
        MaterialDefinition material = (MaterialDefinition) LAYER_MATERIAL_FIELD.get(layer);
        return new ModernModel(
                MATERIAL_WIDTH_FIELD.getInt(material),
                MATERIAL_HEIGHT_FIELD.getInt(material),
                readModernPart("root", mesh.getRoot())
        );
    }

    @SuppressWarnings("unchecked")
    private ModernPart readModernPart(
            String name,
            PartDefinition definition
    ) throws IllegalAccessException {
        List<Object> definitions = (List<Object>) PART_CUBES_FIELD.get(definition);
        List<ModernCube> cubes = new ArrayList<>(definitions.size());
        for (Object cubeDefinition : definitions) {
            Vector3f origin = (Vector3f) CUBE_ORIGIN_FIELD.get(cubeDefinition);
            Vector3f dimensions = (Vector3f) CUBE_DIMENSIONS_FIELD.get(cubeDefinition);
            CubeDeformation deformation = (CubeDeformation) CUBE_DEFORMATION_FIELD.get(
                    cubeDefinition
            );
            UVPair texture = (UVPair) CUBE_TEXTURE_FIELD.get(cubeDefinition);
            cubes.add(new ModernCube(
                    texture.u(),
                    texture.v(),
                    origin.x(),
                    origin.y(),
                    origin.z(),
                    dimensions.x(),
                    dimensions.y(),
                    dimensions.z(),
                    DEFORMATION_X_FIELD.getFloat(deformation),
                    DEFORMATION_Y_FIELD.getFloat(deformation),
                    DEFORMATION_Z_FIELD.getFloat(deformation),
                    CUBE_MIRROR_FIELD.getBoolean(cubeDefinition)
            ));
        }

        Map<String, PartDefinition> childDefinitions =
                (Map<String, PartDefinition>) PART_CHILDREN_FIELD.get(definition);
        List<ModernPart> children = new ArrayList<>(childDefinitions.size());
        for (Map.Entry<String, PartDefinition> entry : childDefinitions.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList()) {
            children.add(readModernPart(entry.getKey(), entry.getValue()));
        }
        return new ModernPart(
                name,
                (PartPose) PART_POSE_FIELD.get(definition),
                List.copyOf(cubes),
                List.copyOf(children)
        );
    }

    private Map<LegacyModelRenderer, String> collectRendererNames(
            Class<?> modelClass,
            LegacyModelBase model
    ) throws IllegalAccessException {
        Map<LegacyModelRenderer, String> names = new IdentityHashMap<>();
        List<Field> fields = Stream.of(modelClass.getFields())
                .filter(field -> field.getType() == LegacyModelRenderer.class)
                .sorted(Comparator.comparing(Field::getName))
                .toList();
        for (Field field : fields) {
            LegacyModelRenderer renderer = (LegacyModelRenderer) field.get(model);
            if (renderer != null) {
                names.putIfAbsent(renderer, field.getName());
            }
        }

        int unnamedIndex = 0;
        List<LegacyModelRenderer> pending = new ArrayList<>(names.keySet());
        for (int index = 0; index < pending.size(); index++) {
            LegacyModelRenderer renderer = pending.get(index);
            for (LegacyModelRenderer child : renderer.getChildren()) {
                if (!names.containsKey(child)) {
                    names.put(child, "unnamed_" + unnamedIndex++);
                    pending.add(child);
                }
            }
        }
        return names;
    }

    private Map<LegacyModelRenderer, LegacyModelRenderer> collectParents(
            Set<LegacyModelRenderer> renderers
    ) {
        Map<LegacyModelRenderer, LegacyModelRenderer> parents = new IdentityHashMap<>();
        for (LegacyModelRenderer renderer : renderers) {
            for (LegacyModelRenderer child : renderer.getChildren()) {
                parents.putIfAbsent(child, renderer);
            }
        }
        return parents;
    }

    private List<String> findModelClasses() throws IOException {
        Set<String> filters = options.models();
        try (Stream<Path> paths = Files.walk(options.sourceRoot())) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith("Model"))
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(path -> !path.toString().contains("legacy"))
                    .map(this::classNameForSource)
                    .filter(className -> filters.isEmpty() || matchesFilter(className, filters))
                    .sorted()
                    .toList();
        }
    }

    private List<Path> findTextures(Path textureRoot) throws IOException {
        if (!Files.isDirectory(textureRoot)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(textureRoot)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png"))
                    .sorted()
                    .toList();
        }
    }

    private int copyBlockAssets(
            Path sourceRoot,
            Path outputRoot,
            String extension
    ) throws IOException {
        if (!Files.isDirectory(sourceRoot)) {
            return 0;
        }

        int copied = 0;
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            for (Path source : paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase(Locale.ROOT)
                            .endsWith(extension))
                    .sorted()
                    .toList()) {
                Path target = outputRoot.resolve(sourceRoot.relativize(source));
                Files.createDirectories(target.getParent());
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                copied++;
            }
        }
        return copied;
    }

    private int copyBlockModels(Path sourceRoot, Path outputRoot) throws IOException {
        if (!Files.isDirectory(sourceRoot)) {
            return 0;
        }

        int copied = 0;
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            for (Path source : paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase(Locale.ROOT)
                            .endsWith(".json"))
                    .sorted()
                    .toList()) {
                JsonObject model = GSON.fromJson(
                        Files.readString(source, StandardCharsets.UTF_8),
                        JsonObject.class
                );
                if (!model.has("display")) {
                    model.add("display", createDefaultBlockDisplay());
                }

                Path target = outputRoot.resolve(sourceRoot.relativize(source));
                Files.createDirectories(target.getParent());
                writeJson(target, model);
                copied++;
            }
        }
        return copied;
    }

    private int exportCompositeBlockModels() throws IOException {
        Path outputDirectory = options.outputRoot().resolve("block");
        Files.createDirectories(outputDirectory);
        exportCompositeBlockModel(
                "DisassemblyStation",
                List.of(
                        new BlockPart("base", "disassembly_station_base", 0.0F, 0.0F, 0.0F),
                        new BlockPart("side", "disassembly_station_side", 16.0F, 0.0F, 0.0F),
                        new BlockPart("top_base", "disassembly_station_top_base", 0.0F, 16.0F, 0.0F),
                        new BlockPart("top_side", "disassembly_station_top_side", 16.0F, 16.0F, 0.0F)
                ),
                outputDirectory.resolve("DisassemblyStation.bbmodel")
        );
        exportCompositeBlockModel(
                "LightsaberForgeLight",
                List.of(
                        new BlockPart("base", "lightsaber_forge_light_base", 0.0F, 0.0F, 0.0F),
                        new BlockPart("panel", "lightsaber_forge_light_panel", 16.0F, 0.0F, 0.0F)
                ),
                outputDirectory.resolve("LightsaberForgeLight.bbmodel")
        );
        exportCompositeBlockModel(
                "LightsaberForgeDark",
                List.of(
                        new BlockPart("base", "lightsaber_forge_dark_base", 0.0F, 0.0F, 0.0F),
                        new BlockPart("panel", "lightsaber_forge_dark_panel", 16.0F, 0.0F, 0.0F)
                ),
                outputDirectory.resolve("LightsaberForgeDark.bbmodel")
        );
        return 3;
    }

    private void exportCompositeBlockModel(
            String name,
            List<BlockPart> parts,
            Path modelFile
    ) throws IOException {
        Map<String, Integer> textureIndexes = new LinkedHashMap<>();
        List<BlockTexture> textures = new ArrayList<>();
        JsonArray elements = new JsonArray();
        JsonArray outliner = new JsonArray();

        for (BlockPart part : parts) {
            ResolvedBlockModel model = resolveBlockModel(part.model());
            JsonObject group = new JsonObject();
            group.addProperty("name", part.name());
            group.add("origin", array(part.offsetX(), part.offsetY(), part.offsetZ()));
            group.addProperty("uuid", stableUuid(name + ":group:" + part.name()));
            group.addProperty("export", true);
            group.addProperty("isOpen", true);
            group.addProperty("locked", false);
            group.addProperty("visibility", true);
            group.addProperty("autouv", 0);
            JsonArray children = new JsonArray();

            for (int index = 0; index < model.elements().size(); index++) {
                JsonObject source = model.elements().get(index).getAsJsonObject();
                String uuid = stableUuid(name + ":" + part.name() + ":cube:" + index);
                elements.add(createCompositeBlockElement(
                        source,
                        model.textures(),
                        textureIndexes,
                        textures,
                        part,
                        uuid,
                        modelFile
                ));
                children.add(uuid);
            }
            group.add("children", children);
            outliner.add(group);
        }

        JsonObject root = new JsonObject();
        JsonObject meta = new JsonObject();
        meta.addProperty("format_version", "4.5");
        meta.addProperty("model_format", "java_block");
        meta.addProperty("box_uv", false);
        root.add("meta", meta);
        root.addProperty("name", name);
        root.addProperty("model_identifier", name);
        root.add("resolution", resolution(16, 16));
        root.add("elements", elements);
        root.add("outliner", outliner);
        root.add("textures", createBlockTextures(name, textures, modelFile));
        root.add("display", createDefaultBlockDisplay());
        writeJson(modelFile, root);
    }

    private JsonObject createCompositeBlockElement(
            JsonObject source,
            Map<String, String> textureVariables,
            Map<String, Integer> textureIndexes,
            List<BlockTexture> textures,
            BlockPart part,
            String uuid,
            Path modelFile
    ) throws IOException {
        JsonArray sourceFrom = source.getAsJsonArray("from");
        JsonArray sourceTo = source.getAsJsonArray("to");
        JsonObject element = new JsonObject();
        element.addProperty("name", part.name() + "_cube");
        element.addProperty("box_uv", false);
        element.addProperty("rescale", false);
        element.addProperty("locked", false);
        element.add("from", offset(sourceFrom, part));
        element.add("to", offset(sourceTo, part));
        element.add("origin", array(
                part.offsetX() + 8.0F,
                part.offsetY() + 8.0F,
                part.offsetZ() + 8.0F
        ));
        element.addProperty("autouv", 0);
        element.addProperty("color", Math.floorMod(part.name().hashCode(), 8));
        element.addProperty("uuid", uuid);

        JsonObject faces = new JsonObject();
        for (Map.Entry<String, com.google.gson.JsonElement> faceEntry
                : source.getAsJsonObject("faces").entrySet()) {
            JsonObject sourceFace = faceEntry.getValue().getAsJsonObject();
            String reference = sourceFace.get("texture").getAsString();
            String textureLocation = resolveTextureReference(reference, textureVariables);
            int textureIndex = textureIndexes.computeIfAbsent(textureLocation, ignored -> {
                try {
                    textures.add(resolveBlockTexture(textureLocation, modelFile));
                    return textures.size() - 1;
                } catch (IOException exception) {
                    throw new IllegalStateException(exception);
                }
            });
            JsonObject face = new JsonObject();
            face.add("uv", sourceFace.has("uv")
                    ? sourceFace.getAsJsonArray("uv").deepCopy()
                    : array(0.0F, 0.0F, 16.0F, 16.0F));
            face.addProperty("texture", textureIndex);
            if (sourceFace.has("rotation")) {
                face.addProperty("rotation", sourceFace.get("rotation").getAsInt());
            }
            faces.add(faceEntry.getKey(), face);
        }
        element.add("faces", faces);
        return element;
    }

    private ResolvedBlockModel resolveBlockModel(String modelName) throws IOException {
        Path path = options.blockModelRoot().resolve(modelName + ".json");
        JsonObject source = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), JsonObject.class);
        Map<String, String> textures = new LinkedHashMap<>();
        JsonArray elements = new JsonArray();
        if (source.has("parent")) {
            String parent = source.get("parent").getAsString();
            String parentName = parent.substring(parent.lastIndexOf('/') + 1);
            ResolvedBlockModel resolvedParent = resolveBlockModel(parentName);
            textures.putAll(resolvedParent.textures());
            elements = resolvedParent.elements().deepCopy();
        }
        if (source.has("textures")) {
            for (Map.Entry<String, com.google.gson.JsonElement> entry
                    : source.getAsJsonObject("textures").entrySet()) {
                textures.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
        if (source.has("elements")) {
            elements = source.getAsJsonArray("elements").deepCopy();
        }
        return new ResolvedBlockModel(textures, elements);
    }

    private String resolveTextureReference(String reference, Map<String, String> variables) {
        String resolved = reference;
        Set<String> visited = new LinkedHashSet<>();
        while (resolved.startsWith("#") && visited.add(resolved)) {
            resolved = variables.getOrDefault(resolved.substring(1), resolved);
        }
        return resolved;
    }

    private BlockTexture resolveBlockTexture(String location, Path modelFile) throws IOException {
        String[] parts = location.split(":", 2);
        String namespace = parts.length == 2 ? parts[0] : "minecraft";
        String texturePath = parts.length == 2 ? parts[1] : parts[0];
        Path path;
        if ("lightsabers".equals(namespace)) {
            path = options.blockTextureRoot().getParent().resolve(texturePath + ".png");
        } else {
            path = extractMinecraftTexture(texturePath, modelFile.getParent().resolve("textures/minecraft"));
        }
        return new BlockTexture(namespace, location, path);
    }

    private Path extractMinecraftTexture(String texturePath, Path outputDirectory) throws IOException {
        String entryName = "assets/minecraft/textures/" + texturePath + ".png";
        Path minecraftJar = Path.of("D:/GradleHome/.gradle/caches/fabric-loom/1.20.1/minecraft-client.jar");
        if (!Files.isRegularFile(minecraftJar)) {
            throw new IOException("Minecraft 1.20.1 client resources not found: " + minecraftJar);
        }
        Path target = outputDirectory.resolve(texturePath + ".png");
        if (Files.isRegularFile(target)) {
            return target;
        }
        Files.createDirectories(target.getParent());
        try (ZipFile zip = new ZipFile(minecraftJar.toFile())) {
            ZipEntry entry = zip.getEntry(entryName);
            if (entry == null) {
                throw new IOException("Missing Minecraft texture " + entryName);
            }
            Files.copy(zip.getInputStream(entry), target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }

    private JsonArray createBlockTextures(
            String modelName,
            List<BlockTexture> blockTextures,
            Path modelFile
    ) throws IOException {
        JsonArray result = new JsonArray();
        for (int index = 0; index < blockTextures.size(); index++) {
            BlockTexture texture = blockTextures.get(index);
            JsonObject entry = new JsonObject();
            entry.addProperty("path", texture.path().toAbsolutePath().toString());
            entry.addProperty("name", texture.path().getFileName().toString());
            entry.addProperty("folder", "");
            entry.addProperty("namespace", texture.namespace());
            entry.addProperty("id", Integer.toString(index));
            entry.addProperty("particle", false);
            entry.addProperty("render_mode", "default");
            entry.addProperty("render_sides", "auto");
            entry.addProperty("frame_time", 1);
            entry.addProperty("frame_order_type", "loop");
            entry.addProperty("frame_order", "");
            entry.addProperty("frame_interpolate", false);
            entry.addProperty("visible", true);
            entry.addProperty("mode", "bitmap");
            entry.addProperty("saved", true);
            entry.addProperty("uuid", stableUuid(modelName + ":texture:" + texture.location()));
            entry.addProperty("relative_path", relativePath(modelFile.getParent(), texture.path()));
            entry.addProperty("source", "data:image/png;base64," + Base64.getEncoder()
                    .encodeToString(Files.readAllBytes(texture.path())));
            result.add(entry);
        }
        return result;
    }

    private JsonObject resolution(int width, int height) {
        JsonObject resolution = new JsonObject();
        resolution.addProperty("width", width);
        resolution.addProperty("height", height);
        return resolution;
    }

    private JsonArray offset(JsonArray source, BlockPart part) {
        return array(
                source.get(0).getAsFloat() + part.offsetX(),
                source.get(1).getAsFloat() + part.offsetY(),
                source.get(2).getAsFloat() + part.offsetZ()
        );
    }

    private JsonObject createDefaultBlockDisplay() {
        JsonObject display = new JsonObject();
        display.add(
                "thirdperson_righthand",
                createDisplayTransform(
                        new float[] {75.0F, 45.0F, 0.0F},
                        new float[] {0.0F, 2.5F, 0.0F},
                        new float[] {0.375F, 0.375F, 0.375F}
                )
        );
        display.add(
                "thirdperson_lefthand",
                createDisplayTransform(
                        new float[] {75.0F, 45.0F, 0.0F},
                        new float[] {0.0F, 2.5F, 0.0F},
                        new float[] {0.375F, 0.375F, 0.375F}
                )
        );
        display.add(
                "firstperson_righthand",
                createDisplayTransform(
                        new float[] {0.0F, 45.0F, 0.0F},
                        null,
                        new float[] {0.4F, 0.4F, 0.4F}
                )
        );
        display.add(
                "firstperson_lefthand",
                createDisplayTransform(
                        new float[] {0.0F, -135.0F, 0.0F},
                        null,
                        new float[] {0.4F, 0.4F, 0.4F}
                )
        );
        display.add(
                "ground",
                createDisplayTransform(
                        null,
                        new float[] {0.0F, 3.0F, 0.0F},
                        new float[] {0.25F, 0.25F, 0.25F}
                )
        );
        display.add(
                "gui",
                createDisplayTransform(
                        new float[] {30.0F, -135.0F, 0.0F},
                        null,
                        new float[] {0.625F, 0.625F, 0.625F}
                )
        );
        display.add(
                "fixed",
                createDisplayTransform(
                        null,
                        null,
                        new float[] {0.5F, 0.5F, 0.5F}
                )
        );
        return display;
    }

    private JsonObject createDisplayTransform(
            float[] rotation,
            float[] translation,
            float[] scale
    ) {
        JsonObject transform = new JsonObject();
        if (rotation != null) {
            transform.add("rotation", floatArray(rotation));
        }
        if (translation != null) {
            transform.add("translation", floatArray(translation));
        }
        if (scale != null) {
            transform.add("scale", floatArray(scale));
        }
        return transform;
    }

    private JsonArray floatArray(float[] values) {
        JsonArray array = new JsonArray();
        for (float value : values) {
            array.add(value);
        }
        return array;
    }

    private void writeBlockAssetReadme() throws IOException {
        Path readme = options.outputRoot().resolve("block_json/README.md");
        Files.createDirectories(readme.getParent());
        Files.writeString(
                readme,
                """
                # 方块 JSON 模型

                该目录中的 `models/block/*.json` 已经是 Minecraft Java 方块模型，可直接使用 Blockbench 的 Java Block/Item 模型格式打开。导出副本会自动补齐 `display`，可在 Blockbench 中调整 GUI、第一/第三人称手持、地面和展示框变换。

                - `lightsaber_stand.json`：光剑展示架/石碑外形
                - `lightsaber_forge_*.json`：光剑锻造台的 base、panel 与阵营材质变体
                - `disassembly_station_*.json`：拆解台的四个多方块组成部分
                - `holocron_*.json`、`lightsaber_crystal.json`：Holocron 与水晶方块模型
                - `sith_coffin.json`、`sith_stone_coffin.json`：仅为方块粒子占位模型，实际外观请编辑 `../../tile/ModelSithCoffin.bbmodel` 与 `../../tile/ModelSithStoneCoffin.bbmodel`

                模组自带的方块纹理位于 `textures/block/`。引用 `minecraft:` 的原版纹理不会复制到这里。
                """.stripIndent(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private List<Path> resolveTextures(String simpleName) {
        String baseName = toSnakeCase(simpleName.substring("Model".length()));
        List<Path> exact = availableTextures.stream()
                .filter(path -> textureStem(path).equals(baseName))
                .toList();
        if (!exact.isEmpty()) {
            return exact;
        }

        List<Path> variants = availableTextures.stream()
                .filter(path -> textureStem(path).startsWith(baseName + "_"))
                .toList();
        if (!variants.isEmpty()) {
            return variants;
        }

        if (baseName.endsWith("_reborn")) {
            return availableTextures.stream()
                    .filter(path -> textureStem(path).equals("reborn"))
                    .toList();
        }
        return List.of();
    }

    private String classNameForSource(Path source) {
        Path relative = options.sourceRoot().relativize(source);
        String suffix = relative.toString()
                .substring(0, relative.toString().length() - ".java".length())
                .replace('/', '.')
                .replace('\\', '.');
        return MODEL_PACKAGE + "." + suffix;
    }

    private Path classOutputDirectory(String className) {
        String packageSuffix = className.substring(MODEL_PACKAGE.length() + 1);
        int lastDot = packageSuffix.lastIndexOf('.');
        if (lastDot < 0) {
            return Path.of("");
        }
        return Path.of(packageSuffix.substring(0, lastDot).replace('.', '/'));
    }

    private static boolean matchesFilter(String className, Set<String> filters) {
        String simpleName = className.substring(className.lastIndexOf('.') + 1);
        return filters.contains(className) || filters.contains(simpleName);
    }

    private static String toSnakeCase(String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("([A-Z])([A-Z][a-z])", "$1_$2")
                .toLowerCase(Locale.ROOT);
    }

    private static String textureStem(Path texture) {
        String fileName = texture.getFileName().toString();
        return fileName.substring(0, fileName.length() - ".png".length());
    }

    private static String stableUuid(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static String relativePath(Path from, Path to) {
        try {
            return from.toAbsolutePath()
                    .normalize()
                    .relativize(to.toAbsolutePath().normalize())
                    .toString()
                    .replace('\\', '/');
        } catch (IllegalArgumentException exception) {
            return to.toAbsolutePath().toString();
        }
    }

    private static Vector toBlockbenchPoint(Vector rawPoint) {
        return new Vector(
                rawPoint.x(),
                BLOCKBENCH_MODEL_Y_ORIGIN - rawPoint.y(),
                rawPoint.z()
        );
    }

    private static JsonArray array(float... values) {
        JsonArray array = new JsonArray();
        for (float value : values) {
            array.add(value == -0.0F ? 0.0F : value);
        }
        return array;
    }

    private record BlockPart(String name, String model, float offsetX, float offsetY, float offsetZ) {
    }

    private record ResolvedBlockModel(Map<String, String> textures, JsonArray elements) {
    }

    private record BlockTexture(String namespace, String location, Path path) {
    }

    private static Field reflectedField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static void writeJson(Path path, JsonObject json) throws IOException {
        Files.writeString(
                path,
                GSON.toJson(json) + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private record Vector(float x, float y, float z) {
        private static final Vector ZERO = new Vector(0.0F, 0.0F, 0.0F);

        Vector add(Vector other) {
            return add(other.x, other.y, other.z);
        }

        Vector add(float addX, float addY, float addZ) {
            return new Vector(x + addX, y + addY, z + addZ);
        }

        JsonArray toJson() {
            return array(x, y, z);
        }
    }

    private record ModernModel(
            int textureWidth,
            int textureHeight,
            ModernPart root
    ) {
    }

    private record ModernPart(
            String name,
            PartPose pose,
            List<ModernCube> cubes,
            List<ModernPart> children
    ) {
    }

    private record ModernCube(
            float textureOffsetX,
            float textureOffsetY,
            float x,
            float y,
            float z,
            float width,
            float height,
            float depth,
            float growX,
            float growY,
            float growZ,
            boolean mirror
    ) {
        boolean hasUniformDeformation() {
            return Float.compare(growX, growY) == 0
                    && Float.compare(growX, growZ) == 0;
        }
    }

    private record Options(
            Path sourceRoot,
            Path textureRoot,
            Path blockModelRoot,
            Path blockTextureRoot,
            Path outputRoot,
            Set<String> models
    ) {
        static Options parse(String[] args) {
            Map<String, String> values = new LinkedHashMap<>();
            Set<String> models = new LinkedHashSet<>();
            for (int index = 0; index < args.length; index += 2) {
                if (index + 1 >= args.length) {
                    throw new IllegalArgumentException("Missing value for " + args[index]);
                }
                String key = args[index];
                String value = args[index + 1];
                if ("--model".equals(key)) {
                    for (String model : value.split(",")) {
                        if (!model.isBlank()) {
                            models.add(model.trim());
                        }
                    }
                } else {
                    values.put(key, value);
                }
            }

            return new Options(
                    requiredPath(values, "--source"),
                    requiredPath(values, "--textures"),
                    requiredPath(values, "--block-models"),
                    requiredPath(values, "--block-textures"),
                    requiredPath(values, "--output"),
                    Set.copyOf(models)
            );
        }

        private static Path requiredPath(Map<String, String> values, String key) {
            String value = values.get(key);
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Missing required argument " + key);
            }
            return Path.of(value).toAbsolutePath().normalize();
        }
    }

    private static final class ExportContext {
        private final String className;
        private final String simpleName;
        private final LegacyModelBase model;
        private final Map<LegacyModelRenderer, String> rendererNames;
        private final List<Path> textures;
        private final Path modelFile;
        private JsonArray mappingNodes;

        private ExportContext(
                String className,
                String simpleName,
                LegacyModelBase model,
                Map<LegacyModelRenderer, String> rendererNames,
                List<Path> textures,
                Path modelFile
        ) {
            this.className = className;
            this.simpleName = simpleName;
            this.model = model;
            this.rendererNames = rendererNames;
            this.textures = textures;
            this.modelFile = modelFile;
        }

        String className() {
            return className;
        }

        String simpleName() {
            return simpleName;
        }

        LegacyModelBase model() {
            return model;
        }

        Map<LegacyModelRenderer, String> rendererNames() {
            return rendererNames;
        }

        List<Path> textures() {
            return textures;
        }

        Path modelFile() {
            return modelFile;
        }

        JsonArray mappingNodes() {
            return mappingNodes;
        }

        void mappingNodes(JsonArray mappingNodes) {
            this.mappingNodes = mappingNodes;
        }
    }

    private static final class ModernExportContext {
        private final String className;
        private final String simpleName;
        private final ModernModel model;
        private final List<Path> textures;
        private final Path modelFile;
        private JsonArray mappingNodes;

        private ModernExportContext(
                String className,
                String simpleName,
                ModernModel model,
                List<Path> textures,
                Path modelFile
        ) {
            this.className = className;
            this.simpleName = simpleName;
            this.model = model;
            this.textures = textures;
            this.modelFile = modelFile;
        }

        String className() {
            return className;
        }

        String simpleName() {
            return simpleName;
        }

        ModernModel model() {
            return model;
        }

        List<Path> textures() {
            return textures;
        }

        Path modelFile() {
            return modelFile;
        }

        JsonArray mappingNodes() {
            return mappingNodes;
        }

        void mappingNodes(JsonArray mappingNodes) {
            this.mappingNodes = mappingNodes;
        }
    }
}
