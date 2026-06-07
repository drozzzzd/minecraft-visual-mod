# ProGuard obfuscation for the Torov Visual Fabric mod jar — LIGHT profile.
#
# Class-name-only obfuscation: class names are renamed and packages flattened,
# but every method and field name is kept. This is deliberately lighter so it
# can NOT break Minecraft callbacks (Screen.render/init/mouseClicked, etc.),
# Mixins, Gson models or the reflective event bus — the previous member-renaming
# profile broke the custom Main Menu because ProGuard could not match the
# intermediary-mapped MC superclasses and renamed the overrides.
#
# No shrinking, no optimization -> zero behaviour change, no runtime cost (no lag).
# Debug info is already stripped at compile time (build.gradle: options.debug = false).

-dontshrink
-dontoptimize
-dontusemixedcaseclassnames

-keepattributes *Annotation*,RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations,AnnotationDefault,Signature,InnerClasses,EnclosingMethod,Exceptions

# Obfuscate ONLY the mod's own packages; everything bundled (Kotlin stdlib, media
# lib, Fabric/MC) is left byte-for-byte intact.
-keep class !powder.**,!torovvisual.** { *; }

# Within the mod packages: keep ALL member NAMES (methods + fields) — only class
# names get obfuscated. This is the core of the light profile; documented idiom
# (-keepclassmembernames) that reliably stops member renaming, so every MC
# callback (Screen.render/init/mouseClicked, ...) survives.
-keepclassmembernames class ** {
    <fields>;
    <methods>;
}

# ── Names that must stay exactly as-is (referenced by name from outside) ──────
# Fabric entrypoint (fabric.mod.json).
-keep class powder.launch.startup.FabricInitializer { *; }
# Mixins (fabric.mixin.json relative names + loom refmap).
-keep class powder.client.mixins.** { *; }
# kotopushka protection SDK (runtime annotations + enums).
-keep class ru.kotopushka.** { *; }
# Every annotation interface definition.
-keep @interface ** { *; }

# Enum plumbing.
-keepclassmembers enum ** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Don't touch resource files (fabric.mod.json, mixin json, refmap, assets,
# protection.key, fonts) — only class identifiers are obfuscated.
-keepdirectories

-dontnote
-dontwarn **
-ignorewarnings
