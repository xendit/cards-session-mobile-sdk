// This is a basic Flutter integration test.
//
// Since integration tests run in a full Flutter application, they can interact
// with the host side of a plugin implementation, unlike Dart unit tests.
//
// For more information about Flutter integration tests, please see
// https://flutter.dev/to/integration-testing

import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';

import 'package:xendit_cards_plugin/xendit_cards_plugin.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('Card session test', (WidgetTester tester) async {
    final plugin = XenditCardsPlugin(apiKey: 'test_key');
    // Add your card session integration tests here
    expect(plugin.apiKey, 'test_key');
  });
}
