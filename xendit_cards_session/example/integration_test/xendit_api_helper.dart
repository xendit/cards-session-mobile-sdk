import 'dart:convert';
import 'package:http/http.dart' as http;

class XenditApiHelper {
  final String apiKey;
  final String baseUrl;
  
  XenditApiHelper({
    required this.apiKey,
    this.baseUrl = 'https://api.stg.tidnex.dev',
  });
  
  // Create a session for card payments
  Future<Map<String, dynamic>> createSession({
    required String referenceId,
    required int amount,
    required String locale,
    required String country,
    required String currency,
    required String customerId,
    required String sessionType,
    String mode = 'API',
    String? description,
    String? cardPaymentTokenId,
    Map<String, dynamic>? metadata,
  }) async {
    final url = Uri.parse('${this.baseUrl}/sessions');
    
    final headers = {
      'Authorization': apiKey.startsWith('Basic ') ? apiKey : 'Basic ${base64Encode(utf8.encode('$apiKey:'))}',
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    };
    
    final body = jsonEncode({
      'mode': 'CARDS_SESSION_JS',
      'amount': amount,
      'locale': locale,
      'country': country,
      'currency': currency,
      'customer_id': customerId,
      'description': description,
      'reference_id': referenceId,
      'session_type': sessionType,
      'channel_properties': {
        'cards': {
          'skip_three_ds': true,
        },
      },
      'cards_session_js': {
        if (cardPaymentTokenId != null) 'card_payment_token_id': cardPaymentTokenId,
        'success_return_url': 'https://yourcompany.com/success',
        'failure_return_url': 'https://yourcompany.com/failure',
      },
      if (metadata != null) 'metadata': metadata,
    });
    
    print('Creating session at URL: $url');
    print('Request body: $body');
    
    try {
      final response = await http.post(
        url,
        headers: headers,
        body: body,
      ).timeout(
        const Duration(seconds: 10),
        onTimeout: () {
          print('HTTP request timed out after 10 seconds');
          throw Exception('Request timed out');
        },
      );
      
      print('Response status: ${response.statusCode}');
      print('Response body: ${response.body}');
      
      if (response.statusCode == 200 || response.statusCode == 201) {
        return jsonDecode(response.body);
      } else {
        throw Exception('Failed to create session: ${response.statusCode} - ${response.body}');
      }
    } catch (e) {
      print('Error in HTTP request: $e');
      throw Exception('Error creating session: $e');
    }
  }
  
  // Extract session ID from session response
  String? extractSessionId(Map<String, dynamic> sessionResponse) {
    // The session ID is typically in the 'id' field
    return sessionResponse['payment_session_id'];
  }
  
  // Get session details
  Future<Map<String, dynamic>> getSession(String sessionId) async {
    final url = Uri.parse('${this.baseUrl}/sessions/$sessionId');
    
    final headers = {
      'Authorization': apiKey.startsWith('Basic ') ? apiKey : 'Basic ${base64Encode(utf8.encode('$apiKey:'))}',
      'Accept': 'application/json',
    };
    
    print('Getting session details from URL: $url');
    
    try {
      final response = await http.get(
        url,
        headers: headers,
      );
      
      print('Get session response status: ${response.statusCode}');
      print('Get session response body: ${response.body}');
      
      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      } else {
        throw Exception('Failed to get session: ${response.statusCode} - ${response.body}');
      }
    } catch (e) {
      print('Error getting session: $e');
      throw Exception('Error getting session: $e');
    }
  }
}