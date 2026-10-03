from transformers import pipeline

models_to_test = [
    "ARTPARK-IISc/DhVaani-0.5",
    "krutrim-ai-labs/Dhwani"
]

for model_id in models_to_test:
    print(f"Testing model: {model_id}")
    try:
        pipe = pipeline("text-to-speech", model=model_id)
        print(f"Successfully loaded {model_id}")
        audio = pipe("ನಮಸ್ಕಾರ")
        print(f"Inference successful: {type(audio)}")
        break
    except Exception as e:
        print(f"Failed to load {model_id}: {e}")

