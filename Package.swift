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
            url: "https://github.com/xendit/cards-session-mobile-sdk/releases/download/1.0.1/CardsSessionMobileSDK-1.0.1.zip",
            checksum: "47697fcb1a6d1418c24d717f547e6e2956dc60b3a7b61c30a03aec90ebc77daa"
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
