# CPhoto
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.geniusrus/cphoto/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.geniusrus/cphoto)

## Short description
Simple way to obtain picture from file system & photo with the Kotlin Coroutines

## Details
If one of the conditions is not valid, then the corresponding exception is thrown:

- ExternalStorageWriteException - if write access to the internal storage is not received
- NotPermissionException - If the permission (API >= 23) to write to memory has been refused
- CancelOperationException - if the user interrupted the operation of receiving photos

Thera are 3 types of returning values:
```
Bitmap,
Uri,
String (absolute path to file)
```

There are 5 types to obtaining images (TypeRequest):
```
CAMERA, - only from camera
GALLERY, - only from gallery
COMBINE, - combining two previous types
COMBINE_MULTIPLE, - same as COMBINE, but support multiple selection, requires API 16
FROM_DOCUMENT; - using Storage Access Framework, requieres API 19
```

## Usage
* Get photo from camera
```kotlin
val imageFromCamera = CRPhoto(context).requestBitmap(TypeRequest.CAMERA)
```

* Is equal to
```kotlin
val imageFromCamera = this@context takePhotoBitmap TypeRequest.CAMERA
```

## Install
Add to your .gradle file:
```gradle
implementation 'com.geniusrus.cphoto:cphoto:$latest_version'
```
## Sample
The sample is on `app` module

## Developed by 
* Viktor Likhanov

Yandex: [Gen1usRUS@yandex.ru](mailto:Gen1usRUS@yandex.ru)

## License
```
Apache v2.0 License

Copyright (c) 2018 Viktor Likhanov
