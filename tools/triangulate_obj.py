from __future__ import annotations

import argparse
from pathlib import Path


EPSILON = 1.0e-9


def vertex_index(token: str, vertex_count: int) -> int:
    raw_index = int(token.split("/", 1)[0])
    return raw_index - 1 if raw_index > 0 else vertex_count + raw_index


def project_polygon(points: list[tuple[float, float, float]]) -> list[tuple[float, float]]:
    normal_x = 0.0
    normal_y = 0.0
    normal_z = 0.0
    for index, current in enumerate(points):
        following = points[(index + 1) % len(points)]
        normal_x += (current[1] - following[1]) * (current[2] + following[2])
        normal_y += (current[2] - following[2]) * (current[0] + following[0])
        normal_z += (current[0] - following[0]) * (current[1] + following[1])

    dominant_axis = max(
        range(3),
        key=lambda axis: abs((normal_x, normal_y, normal_z)[axis]),
    )
    if dominant_axis == 0:
        return [(point[1], point[2]) for point in points]
    if dominant_axis == 1:
        return [(point[0], point[2]) for point in points]
    return [(point[0], point[1]) for point in points]


def cross(
    start: tuple[float, float],
    end: tuple[float, float],
    point: tuple[float, float],
) -> float:
    return (end[0] - start[0]) * (point[1] - start[1]) - (
        end[1] - start[1]
    ) * (point[0] - start[0])


def signed_area(points: list[tuple[float, float]]) -> float:
    return sum(
        current[0] * points[(index + 1) % len(points)][1]
        - points[(index + 1) % len(points)][0] * current[1]
        for index, current in enumerate(points)
    )


def point_in_triangle(
    point: tuple[float, float],
    first: tuple[float, float],
    second: tuple[float, float],
    third: tuple[float, float],
    orientation: float,
) -> bool:
    return (
        orientation * cross(first, second, point) > EPSILON
        and orientation * cross(second, third, point) > EPSILON
        and orientation * cross(third, first, point) > EPSILON
    )


def triangulate_face(
    tokens: list[str],
    positions: list[tuple[float, float, float]],
) -> tuple[list[list[str]], bool]:
    if len(tokens) <= 4:
        return [tokens], False

    points = [positions[vertex_index(token, len(positions))] for token in tokens]
    projected = project_polygon(points)
    cleaned_tokens: list[str] = []
    cleaned_points: list[tuple[float, float]] = []
    for token, point in zip(tokens, projected):
        if cleaned_points and abs(cross(cleaned_points[-1], point, point)) <= EPSILON:
            if abs(point[0] - cleaned_points[-1][0]) <= EPSILON and abs(
                point[1] - cleaned_points[-1][1]
            ) <= EPSILON:
                continue
        cleaned_tokens.append(token)
        cleaned_points.append(point)
    if len(cleaned_points) > 2:
        changed = True
        while changed and len(cleaned_points) > 3:
            changed = False
            for index in range(len(cleaned_points)):
                previous = cleaned_points[index - 1]
                current = cleaned_points[index]
                following = cleaned_points[(index + 1) % len(cleaned_points)]
                if abs(cross(previous, current, following)) <= EPSILON:
                    del cleaned_points[index]
                    del cleaned_tokens[index]
                    changed = True
                    break
    tokens = cleaned_tokens
    projected = cleaned_points
    if len(tokens) <= 4:
        return [tokens], False
    orientation = 1.0 if signed_area(projected) >= 0.0 else -1.0
    remaining = list(range(len(tokens)))
    triangles: list[list[str]] = []

    while len(remaining) > 3:
        ear_found = False
        for slot, current_index in enumerate(remaining):
            previous_index = remaining[slot - 1]
            next_index = remaining[(slot + 1) % len(remaining)]
            first = projected[previous_index]
            second = projected[current_index]
            third = projected[next_index]
            if orientation * cross(first, second, third) <= EPSILON:
                continue
            if any(
                point_in_triangle(
                    projected[test_index],
                    first,
                    second,
                    third,
                    orientation,
                )
                for test_index in remaining
                if test_index not in (previous_index, current_index, next_index)
            ):
                continue
            triangles.append(
                [tokens[previous_index], tokens[current_index], tokens[next_index]]
            )
            del remaining[slot]
            ear_found = True
            break

        if not ear_found:
            return [
                [tokens[0], tokens[index], tokens[index + 1]]
                for index in range(1, len(tokens) - 1)
            ], True

    triangles.append([tokens[index] for index in remaining])
    return triangles, False


def triangulate_obj(path: Path) -> tuple[int, int]:
    positions: list[tuple[float, float, float]] = []
    output: list[str] = []
    converted_faces = 0
    fallback_faces = 0

    for line in path.read_text(encoding="utf-8").splitlines():
        if line.startswith("v "):
            coordinates = line.split()
            positions.append(tuple(float(value) for value in coordinates[1:4]))
            output.append(line)
            continue
        if not line.startswith("f "):
            output.append(line)
            continue

        face_tokens = line.split()[1:]
        triangles, used_fallback = triangulate_face(face_tokens, positions)
        output.extend("f " + " ".join(triangle) for triangle in triangles)
        if len(face_tokens) > 4:
            converted_faces += 1
            fallback_faces += int(used_fallback)

    path.write_text("\n".join(output) + "\n", encoding="utf-8", newline="\n")
    return converted_faces, fallback_faces


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("paths", nargs="+", type=Path)
    args = parser.parse_args()
    for path in args.paths:
        converted, fallbacks = triangulate_obj(path)
        print(f"{path.name}: triangulated={converted}, fallbacks={fallbacks}")


if __name__ == "__main__":
    main()
