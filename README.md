# AlarmManagerCompat
[![Release](https://jitpack.io/v/rzetzsche/alarmmanagercompat.svg)](https://jitpack.io/#rzetzsche/alarmmanagercompat)

Library to port old alarm manager behaviour to new android versions.

# Set Up

Add following to your build.gradle:
```
repositories {
    maven { url "https://jitpack.io" }
}
dependencies {
    compile 'com.github.rzetzsche:alarmmanagercompat:0.1'
}
```

# Usage

```
AlarmManagerCompat alarmManagerCompat = AlarmManagerCompat.getInstance(getApplicationContext());
```

