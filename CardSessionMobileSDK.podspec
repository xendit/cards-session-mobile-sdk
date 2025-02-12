Pod::Spec.new do |spec|
    spec.name                     = 'CardSessionMobileSDK'
    spec.version                  = '1.0.1'
    spec.homepage                 = 'https://github.com/xendit/cards-session-mobile-sdk'
    spec.source                   = { :http=> 'https://github.com/xendit/cards-session-mobile-sdk/releases/download/1.0.1/CardsSessionMobileSDK-1.0.1.zip'}
    spec.author                   = { "mobile-sdk-team" => "mobile-sdk@xendit.co" }
    spec.license                  = { :type => 'MIT', :text => ''}
    spec.summary                  = 'Xendit\'s Cards Session SDK module'
    spec.libraries                = 'c++'
    spec.platform                 = :ios, '14.0'
    spec.ios.deployment_target    = '14.0'
    spec.vendored_frameworks      = 'cardsSdk.xcframework'
    spec.dependency 'XenditFingerprintSDK', '1.0.1'
end
