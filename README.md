# Minecraft 1.16.2 OpenJ9 Compatibility

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
