import 'package:flutter/material.dart';
import 'dart:async';

import 'package:xendit_cards_session/xendit_cards_session.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Xendit Cards Session Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const CardsSessionPage(),
    );
  }
}

class CardsSessionPage extends StatefulWidget {
  const CardsSessionPage({super.key});

  @override
  State<CardsSessionPage> createState() => _CardsSessionPageState();
}

class _CardsSessionPageState extends State<CardsSessionPage> {
  final _xenditCardsSession = XenditCardsSession();
  final _paymentSessionIdController = TextEditingController();
  bool _isConfirmedSaved = false;
  CardSessionState _state = CardSessionState();
  
  @override
  void initState() {
    super.initState();
    _initializeSDK();
    _xenditCardsSession.state.listen((state) {
      setState(() {
        _state = state;
      });
    });
  }

  Future<void> _initializeSDK() async {
    await _xenditCardsSession.initialize(apiKey: "xnd_public_development_YOUR_KEY_HERE");
  }

  @override
  void dispose() {
    _paymentSessionIdController.dispose();
    _xenditCardsSession.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Xendit Cards Session'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              const SizedBox(height: 32),
              const Text(
                'Welcome to Cards Session!',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
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
              const SizedBox(height: 8),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Text(
                    'Confirm Save',
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(width: 16),
                  Switch(
                    value: _isConfirmedSaved,
                    onChanged: (value) {
                      setState(() {
                        _isConfirmedSaved = value;
                      });
                    },
                  ),
                ],
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () async {
                  try {
                    await _xenditCardsSession.collectCardData(
                      cardNumber: "4242424242424242",
                      expiryMonth: "12",
                      expiryYear: "2026",
                      cvn: null,
                      cardholderFirstName: "First",
                      cardholderLastName: "Name",
                      cardholderEmail: "firstname@xendit.co",
                      cardholderPhoneNumber: "+123456789",
                      paymentSessionId: _paymentSessionIdController.text,
                      confirmSave: _isConfirmedSaved,
                      billingInformation: const BillingInformationDto(
                        firstName: "Budi",
                        lastName: "Santoso",
                        email: "budi@example.co.id",
                        phoneNumber: "+6281234567890",
                        streetLine1: "Jl. Jend. Sudirman No.Kav 48A",
                        streetLine2: "",
                        city: "Jakarta",
                        provinceState: "DKI Jakarta",
                        country: "ID",
                        postalCode: "12190"
                      ),
                    );
                  } catch (e) {
                    // Error is handled via state stream
                  }
                },
                child: const Text('Collect Card Data'),
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () async {
                  try {
                    await _xenditCardsSession.collectCvn(
                      cvn: "123",
                      paymentSessionId: _paymentSessionIdController.text,
                    );
                  } catch (e) {
                    // Error is handled via state stream
                  }
                },
                child: const Text('Collect CVN'),
              ),
              const SizedBox(height: 16),
              _buildStateWidget(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStateWidget() {
    if (_state.isLoading) {
      return const CircularProgressIndicator();
    } else if (_state.exception != null) {
      final errorMessage = _state.exception!.message ?? 
                          'Error: ${_state.exception!.errorCode}';
      return Text(
        errorMessage,
        style: const TextStyle(color: Colors.red),
      );
    } else if (_state.cardResponse != null) {
      return Text(_state.cardResponse!.message ?? 'Success');
    }
    return const SizedBox.shrink();
  }
}
