// swift-tools-version:5.9.0
import PackageDescription

let package = Package(
    name: "CardsSessionMobileSDK",
    platforms: [
        .iOS(.v14),
    ],
    products: [
        .library(name: "CardsSessionMobileSDK", targets: ["CardsSessionMobileSDKTarget"])
    ],
    targets: [
        .binaryTarget(
            name: "CardsSessionMobileSDK",
            url: "https://github.com/xendit/cards-session-mobile-sdk/releases/download/1.1.0/CardsSessionMobileSDK-1.1.0.zip",
            checksum: "d94e96fad6b551d536aedbc70de6ea34c8c0985a0a5b9d94c9e139eb7e44b6f1"
        ),
        .binaryTarget(
            name: "XenditFingerprintSDK",
            url: "https://cdn-xenshield.xendit.co/fingerprint-sdk/ios/1.0.1/XenditFingerprintSDK-1.0.1.zip",
            checksum: "d8dbb2e00525eb7765972e10aa0cf49d990a7cc40ddff05d0f620a17b487ceb0"
        ),
        .target(
            name: "CardsSessionMobileSDKTarget",
            dependencies: [
                .target(name: "CardsSessionMobileSDK"),
                .target(name: "XenditFingerprintSDK")
            ],
            path:"CardsSessionMobileSDKTarget",
            linkerSettings: [
                    .linkedFramework("SystemConfiguration"),
                    .linkedFramework("CoreTelephony", .when(platforms: [.iOS]))
                ]
        )
    ]
)
