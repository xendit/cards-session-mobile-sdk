import 'package:flutter_test/flutter_test.dart';
import 'package:xendit_cards_session/src/utils/credit_card_util.dart';

void main() {
  group('CreditCardUtil', () {
    group('isCreditCardNumberValid', () {
      test('should return true for valid card number', () {
        expect(CreditCardUtil.isCreditCardNumberValid('4111111111111111'), true);
      });

      test('should return false for empty card number', () {
        expect(CreditCardUtil.isCreditCardNumberValid(''), false);
      });

      test('should return false for whitespace-only card number', () {
        expect(CreditCardUtil.isCreditCardNumberValid('   '), false);
      });
    });

    group('isCreditCardExpirationDateValid', () {
      test('should return true for valid month and year', () {
        // Use a future year to ensure the test doesn't fail as time passes
        final futureYear = DateTime.now().year + 1;
        expect(CreditCardUtil.isCreditCardExpirationDateValid('12', futureYear.toString()), true);
      });

      test('should return false for invalid month', () {
        final futureYear = DateTime.now().year + 1;
        expect(CreditCardUtil.isCreditCardExpirationDateValid('13', futureYear.toString()), false);
        expect(CreditCardUtil.isCreditCardExpirationDateValid('0', futureYear.toString()), false);
      });

      test('should return false for past year', () {
        final pastYear = DateTime.now().year - 1;
        expect(CreditCardUtil.isCreditCardExpirationDateValid('12', pastYear.toString()), false);
      });

      test('should return false for non-numeric input', () {
        expect(CreditCardUtil.isCreditCardExpirationDateValid('ab', '20cd'), false);
      });
    });

    group('isCreditCardCVNValid', () {
      test('should return true for valid 3-digit CVN', () {
        expect(CreditCardUtil.isCreditCardCVNValid('123'), true);
      });

      test('should return true for valid 4-digit CVN', () {
        expect(CreditCardUtil.isCreditCardCVNValid('1234'), true);
      });

      test('should return true for null CVN (optional)', () {
        expect(CreditCardUtil.isCreditCardCVNValid(null), true);
      });

      test('should return false for empty CVN', () {
        expect(CreditCardUtil.isCreditCardCVNValid(''), false);
      });

      test('should return false for CVN with non-digit characters', () {
        expect(CreditCardUtil.isCreditCardCVNValid('12a'), false);
      });

      test('should return false for CVN with wrong length', () {
        expect(CreditCardUtil.isCreditCardCVNValid('12'), false);
        expect(CreditCardUtil.isCreditCardCVNValid('12345'), false);
      });
    });
  });
} 