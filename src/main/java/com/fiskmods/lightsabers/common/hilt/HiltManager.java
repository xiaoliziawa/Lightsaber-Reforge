package com.fiskmods.lightsabers.common.hilt;

public final class HiltManager {
    public static final Hilt GRAFLEX = new HiltGraflex();
    public static final Hilt REDEEMER = new HiltRedeemer();
    public static final Hilt MAULER = new HiltMauler();
    public static final Hilt PRODIGAL_SON = new HiltProdigalSon();
    public static final Hilt KNIGHTED = new HiltKnighted();
    public static final Hilt VAID_ANCIENT = new HiltVaid();
    public static final Hilt VAID_MODERN = new HiltVaid();
    public static final Hilt DROIDEKA = new HiltDroideka();
    public static final Hilt FULCRUM = new HiltFulcrum();
    public static final Hilt JUGGERNAUT = new HiltJuggernaut();
    public static final Hilt MECHANICAL = new HiltMechanical();
    public static final Hilt MANDALORIAN = new HiltMandalorian();
    public static final Hilt FURY = new HiltFury();
    public static final Hilt REBEL = new HiltRebel();
    public static final Hilt IMPERIAL = new HiltImperial();
    public static final Hilt REBORN = new HiltReborn();
    public static final Hilt SPINNING = new HiltSpinning();
    public static final Hilt SPEAR = new HiltSpear();

    private HiltManager() {
    }

    public static void register() {
        Hilt.register("graflex", GRAFLEX);
        Hilt.register("redeemer", REDEEMER);
        Hilt.register("mauler", MAULER);
        Hilt.register("prodigal_son", PRODIGAL_SON);
        Hilt.register("knighted", KNIGHTED);
        Hilt.register("vaid_ancient", VAID_ANCIENT);
        Hilt.register("vaid_modern", VAID_MODERN);
        Hilt.register("droideka", DROIDEKA);
        Hilt.register("fulcrum", FULCRUM);
        Hilt.register("juggernaut", JUGGERNAUT);
        Hilt.register("mechanical", MECHANICAL);
        Hilt.register("mandalorian", MANDALORIAN);
        Hilt.register("fury", FURY);
        Hilt.register("rebel", REBEL);
        Hilt.register("imperial", IMPERIAL);
        Hilt.register("reborn", REBORN);
        Hilt.register("spinning", SPINNING);
        Hilt.register("spear", SPEAR);

        map(GRAFLEX, "Graflex");
        map(REDEEMER, "Redeemer");
        map(MAULER, "Mauler");
        map(PRODIGAL_SON, "Prodigal Son");
        map(KNIGHTED, "Knighted");
        map(VAID_ANCIENT, "Vaid (Ancient)");
        map(VAID_MODERN, "Vaid (Modern)");
        map(DROIDEKA, "Droideka");
        map(FULCRUM, "Fulcrum");
        map(JUGGERNAUT, "Juggernaut");
        map(MECHANICAL, "Mechanical");
        map(MANDALORIAN, "Mandalorian");
        map(FURY, "Fury");
    }

    private static void map(Hilt value, String legacy) {
        Hilt.LEGACY_MAPPINGS.put(legacy, value.delegate.name());
    }
}
