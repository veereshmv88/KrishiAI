import os
from whisper_service import WhisperService

def create_dummy_wav(filepath: str):
    import numpy as np
    import soundfile as sf
    # Create 1 second of random noise
    data = np.random.uniform(-1, 1, 16000)
    sf.write(filepath, data, 16000)
    print(f"Created dummy WAV: {filepath}")

def run_tests():
    print("Initializing WhisperService...")
    service = WhisperService()
    
    # Create test files
    create_dummy_wav("english_test.wav")
    create_dummy_wav("kannada_test.wav")
    create_dummy_wav("mixed_test.wav")
    
    with open("empty.wav", "wb") as f:
        pass
        
    with open("invalid.txt", "w") as f:
        f.write("This is not an audio file")

    print("\n--- Testing English WAV ---")
    res = service.transcribe("english_test.wav", "en")
    print(f"Success: {res.success}, Text: '{res.text}', Language: {res.language}, Error: {res.error}")

    print("\n--- Testing Kannada WAV ---")
    res = service.transcribe("kannada_test.wav", "kn")
    print(f"Success: {res.success}, Text: '{res.text}', Language: {res.language}, Error: {res.error}")

    print("\n--- Testing Mixed WAV (Auto) ---")
    res = service.transcribe("mixed_test.wav", "auto")
    print(f"Success: {res.success}, Text: '{res.text}', Language: {res.language}, Error: {res.error}")

    print("\n--- Testing Empty Audio ---")
    res = service.transcribe("empty.wav", "auto")
    print(f"Success: {res.success}, Error: {res.error}")
    
    print("\n--- Testing Invalid Audio Format ---")
    res = service.transcribe("invalid.txt", "auto")
    print(f"Success: {res.success}, Error: {res.error}")
    
    print("\n--- Testing Missing File ---")
    res = service.transcribe("does_not_exist.wav", "auto")
    print(f"Success: {res.success}, Error: {res.error}")

    # Cleanup
    for f in ["english_test.wav", "kannada_test.wav", "mixed_test.wav", "empty.wav", "invalid.txt"]:
        if os.path.exists(f):
            os.remove(f)

if __name__ == "__main__":
    run_tests()
