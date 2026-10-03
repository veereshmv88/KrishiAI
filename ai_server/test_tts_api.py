import requests

def test_tts():
    print("Testing English TTS...")
    response = requests.post(
        "http://localhost:8000/tts",
        json={
            "text": "Hello, welcome to KrishiAI.",
            "language": "en"
        }
    )
    if response.status_code == 200:
        with open("test_api_en.wav", "wb") as f:
            f.write(response.content)
        print("English TTS SUCCESS! Saved to test_api_en.wav")
    else:
        print(f"English TTS FAILED: {response.text}")

    print("Testing Kannada TTS...")
    response = requests.post(
        "http://localhost:8000/tts",
        json={
            "text": "ನಮಸ್ಕಾರ, ಕೃಷಿAI ಗೆ ಸ್ವಾಗತ.",
            "language": "kn"
        }
    )
    if response.status_code == 200:
        with open("test_api_kn.wav", "wb") as f:
            f.write(response.content)
        print("Kannada TTS SUCCESS! Saved to test_api_kn.wav")
    else:
        print(f"Kannada TTS FAILED: {response.text}")

if __name__ == "__main__":
    test_tts()
