# Minecraft 1.16.2 OpenJ9 Compatibility

## Note

This has been fixed in upstream OpenJ9 release
[0.23.0](https://github.com/eclipse/openj9/releases/tag/openj9-0.23.0)
(commit [ac3cd72](https://github.com/eclipse/openj9/commit/ac3cd7299d79258701a88196600e6fa3c42a0021)).
This release has been built by [AdoptOpenJDK](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=openj9).
It is no longer necessary to use this mod for 1.16.2 or any future Minecraft
versions if you are using OpenJ9 >=0.23.0.

Allows running Minecraft 1.16.2 using the more memory-efficient OpenJ9 VM.

## Technical details

The default HotSpot VM does static initialization lazily, that is, only as
soon as it is needed. OpenJ9 on the other hand will do static initialization
as soon as a class is referenced. Minecraft depends on this lazy initialization
behavior in `BuiltinRegistriesMixin` as it will first obtain method references
for needed methods and then run them as soon as it finishes initializing required
static fields. However, the two methods used (`StructurePools::initDefaultPools`
and `ChunkGeneratorSettings::getInstance`) belong to classes with their own
static initializers. Those initializers require some static fields to be complete
in `BuiltinRegistriesMixin`. In HotSpot, the static initializers are run when
the method references are invoked for the first time. In comparison, OpenJ9 will
run the static initializers of these classes as soon as the method references
are obtained. This results in a `NullPointerException` as those blocks attempt
to reference not-yet-assigned values in `BuiltinRegistriesMixin`.

This mod simply postpones static initialization of the problematic classes by
manually running their initialization methods later.
