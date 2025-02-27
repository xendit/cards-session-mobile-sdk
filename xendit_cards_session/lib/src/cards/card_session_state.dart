import 'package:meta/meta.dart';
import '../models/cards_session_dto.dart';
import '../network/cards_session_exception.dart';

/// Represents the current state of a card session
@immutable
class CardSessionState {
  final bool isLoading;
  final CardsResponseDto? cardResponse;
  final CardsSessionException? exception;

  const CardSessionState({
    this.isLoading = false,
    this.cardResponse,
    this.exception,
  });

  // operator and hashCode will make these value objects
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is CardSessionState &&
          runtimeType == other.runtimeType &&
          isLoading == other.isLoading &&
          cardResponse == other.cardResponse &&
          exception == other.exception;

  @override
  int get hashCode => 
      isLoading.hashCode ^ 
      (cardResponse?.hashCode ?? 0) ^ 
      (exception?.hashCode ?? 0);
} 