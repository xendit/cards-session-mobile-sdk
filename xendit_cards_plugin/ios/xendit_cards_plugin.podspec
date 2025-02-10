#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint xendit_cards_plugin.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'xendit_cards_plugin'
  s.version          = '1.0.0'
  s.homepage         = 'https://github.com/xendit/xendit-cards-plugin'
  s.source           = { :path => '.' }
  s.author           = { 'Xendit' => 'support@xendit.co' }
  s.license          = { :type => 'MIT', :text => 'License text'}
  s.summary          = 'Xendit Cards Plugin for Flutter'
  s.description      = 'Flutter plugin for integrating Xendit Cards SDK'
  s.source_files     = 'Classes/**/*'
  s.dependency 'Flutter'
  s.dependency 'cardsSdk', '1.0.0'
  s.ios.deployment_target = '14.0'

  s.pod_target_xcconfig = { 
    'DEFINES_MODULE' => 'YES',
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386',
    'ENABLE_USER_SCRIPT_SANDBOXING' => 'NO'
  }
  s.swift_version = '5.0'

  # If your plugin requires a privacy manifest, for example if it uses any
  # required reason APIs, update the PrivacyInfo.xcprivacy file to describe your
  # plugin's privacy impact, and then uncomment this line. For more information,
  # see https://developer.apple.com/documentation/bundleresources/privacy_manifest_files
  # s.resource_bundles = {'xendit_cards_plugin_privacy' => ['Resources/PrivacyInfo.xcprivacy']}
end
