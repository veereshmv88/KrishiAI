import os
import time
import numpy as np
import soundfile as sf

def report_wav(filename, language):
    if not os.path.exists(filename):
        print(f"DHVAANI_{language.upper()}_SYNTHESIS = FAILED (File not found)")
        return False
        
    try:
        data, samplerate = sf.read(filename)
        duration = len(data) / samplerate
        min_amp = np.min(data)
        max_amp = np.max(data)
        rms = np.sqrt(np.mean(data**2))
        
        print(f"--- {language.upper()} WAV REPORT ---")
        print(f"File: {filename}")
        print(f"Duration: {duration:.2f} seconds")
        print(f"Sample Rate: {samplerate} Hz")
        print(f"Samples: {len(data)}")
        print(f"Min Amplitude: {min_amp:.4f}")
        print(f"Max Amplitude: {max_amp:.4f}")
        print(f"RMS Amplitude: {rms:.4f}")
        
        if duration > 0 and rms > 0.0001:
            print(f"DHVAANI_{language.upper()}_SYNTHESIS = PASS\n")
            return True
        else:
            print(f"DHVAANI_{language.upper()}_SYNTHESIS = FAILED (Silent or empty)\n")
            return False
    except Exception as e:
        print(f"DHVAANI_{language.upper()}_SYNTHESIS = FAILED (Error reading file: {e})\n")
        return False

print("=== PHASE H: REAL SYNTHESIS TEST ===")
en_pass = report_wav("standalone_english.wav", "ENGLISH")
kn_pass = report_wav("standalone_kannada.wav", "KANNADA")

if en_pass and kn_pass:
    print("DHVAANI_CPU_INFERENCE = PASS")
    print("DHVAANI_INFERENCE = PASS")
else:
    print("DHVAANI_INFERENCE = FAILED")
