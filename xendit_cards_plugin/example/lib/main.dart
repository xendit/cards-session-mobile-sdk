import 'package:flutter/material.dart';
import 'package:xendit_cards_plugin/xendit_cards_plugin.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
      ),
      home: const CardsSessionScreen(),
    );
  }
}

class CardsSessionScreen extends StatefulWidget {
  const CardsSessionScreen({super.key});

  @override
  State<CardsSessionScreen> createState() => _CardsSessionScreenState();
}

class _CardsSessionScreenState extends State<CardsSessionScreen> {
  final _xenditCardsPlugin = XenditCardsPlugin(apiKey: 'xnd_public_development_YOUR_KEY_HERE');
  final _paymentSessionIdController = TextEditingController();
  String? _responseMessage;
  bool _isLoading = false;
  String? _errorMessage;
  bool _confirmSave = false;

  Future<void> _collectCardData() async {
    if (_paymentSessionIdController.text.isEmpty) {
      setState(() {
        _errorMessage = 'Payment Session ID is required';
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
      _responseMessage = null;
    });

    try {
      final response = await _xenditCardsPlugin.collectCardData(
        cardNumber: '4242424242424242',
        expiryMonth: '12',
        expiryYear: '2026',
        firstName: 'First',
        lastName: 'Name',
        email: 'firstname@xendit.co',
        phoneNumber: '+123456789',
        paymentSessionId: _paymentSessionIdController.text,
        confirmSave: _confirmSave,
      );

      setState(() {
        _responseMessage = response.toString();
      });
    } catch (e) {
      setState(() {
        _errorMessage = e.toString();
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _collectCvn() async {
    if (_paymentSessionIdController.text.isEmpty) {
      setState(() {
        _errorMessage = 'Payment Session ID is required';
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
      _responseMessage = null;
    });

    try {
      final response = await _xenditCardsPlugin.collectCvn(
        cvn: '123',
        paymentSessionId: _paymentSessionIdController.text,
      );

      setState(() {
        _responseMessage = response.toString();
      });
    } catch (e) {
      setState(() {
        _errorMessage = e.toString();
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Cards Session Example'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text(
              'Welcome to Cards Session!',
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 20),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _paymentSessionIdController,
              decoration: const InputDecoration(
                labelText: 'Enter paymentSessionId',
                hintText: 'paymentSessionId',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                const Text(
                  'Confirm Save',
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
                const SizedBox(width: 8),
                Switch(
                  value: _confirmSave,
                  onChanged: (bool value) {
                    setState(() {
                      _confirmSave = value;
                    });
                  },
                ),
              ],
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _isLoading ? null : _collectCardData,
              child: const Text('Collect Card Data'),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _isLoading ? null : _collectCvn,
              child: const Text('Collect CVN'),
            ),
            const SizedBox(height: 16),
            if (_isLoading)
              const Center(child: CircularProgressIndicator())
            else if (_errorMessage != null)
              Text(
                _errorMessage!,
                style: const TextStyle(color: Colors.red),
                textAlign: TextAlign.center,
              )
            else if (_responseMessage != null)
              Text(
                _responseMessage!,
                style: const TextStyle(color: Colors.green),
                textAlign: TextAlign.center,
              ),
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    _paymentSessionIdController.dispose();
    super.dispose();
  }
}
