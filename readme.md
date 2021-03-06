# Retrograd [ ![Download](https://api.bintray.com/packages/retrograd/Retrograd/retrograd/images/download.svg) ](https://bintray.com/retrograd/Retrograd/retrograd/_latestVersion)
Retrograd is networking library for JSON RPC 2.0 services. Inspired by Retrofit. Uses reflection under the hood.

## Installation
You should add repository with this library to your repositories list. For Gradle
```groovy
repositories {
    // ...
    jcenter()
}
```
Android users should place this into the `allprojects` section of base `build.gradle` file.

And add dependency
```groovy
dependencies {
    implementation 'com.github.mikhail57:Retrograd:0.3.1'
}
```
Please, check current version of the library at [Bintray](https://bintray.com/retrograd/Retrograd/retrograd)!

For the other build systems guides, please visit [Bintray](https://bintray.com/retrograd/Retrograd/retrograd)

## Current limitations
- Supports only `Single<T>` as a return type
- Supports only `GSON` as a converter
- Current library API is under construction, so there might be some changes over 0.x versions
- Not full support of Kotlin language

## Usage example
### Interface
Interface for JSON RPC 2.0 API endpoint
```kotlin
@JsonRpcSerivce("inquiry/rpc")
interface InquiryApi {
    @JsonRpcMethod("get")
    fun getInquiry(
            @JsonRpcParam("sessionId") sessionId: String,
            @JsonRpcParam("service") service: String,
            @JsonRpcParam("params") params: Map<String, Any>
    ): Single<InquiryResult>
}
```

`@JsonRpcSerivce("inquiry/rpc")` - required annotation for the `interface`. As an optional parameter you can provide URL to be 
resolved against `baseUrl` (described below).

`@JsonRpcMethod("get")` - annotation to mark current method as JSON RPC method. Return type of the function marked with 
this annotation **currently** should be `Single<*>`. As the parameters there is parameters for remote method (will be
used in JSON RPC 2.0 request). You can specify, whenever this remote procedure is accepting **named** or **unnamed**
params, by specifying `namedParams` param of this annotation. When it's `true`, method will be invoked with named params,
otherwise with unnamed params.

`@JsonRpcParam("sessionId")` - annotation used to mark current param as a named param in JSON RPC 2.0 method call.
As an annotation parameter there is a name of this named param. RPC method with unnamed params will ignore name.

### Retrograd object creation
```kotlin
val retrograd = Retrograd.Builder()
            .baseUrl(ApiConfig.getServerUrl() + "/api/v1.1/")
            .gson(gson)
            .client(okHttpClient)
            .build()
```
There is a builder to build Retrograd object. You can specify `GSON` and `OkHttpClient` that will be used over the request.

### Retrograd client creation
To create API instance, you should call `create` method of `Retrograd`. For example
```kotlin
val api: InquiryApi = retrograd.create(InquiryApi::class.java)
```

### Kotlin extensions
You can create API instance via Kotlin extension function, like
```kotlin
val api: InquiryApi = retrograd.create()
```
