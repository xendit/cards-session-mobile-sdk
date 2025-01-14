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
            url: "https://github.com/xendit/cards-session-mobile-sdk/releases/download/1.0.0/CardsSessionMobileSDK-1.0.0.zip",
            checksum: "ece9e7e73dffcdec868f7753a58544bb12d157ca4970b6816a9a3721f4e59827"
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
