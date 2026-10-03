import urllib.request
import os
import zipfile

assets_dir = r"E:\Codes\Templates\Farmer\app\src\main\assets\models"
os.makedirs(assets_dir, exist_ok=True)

url = "https://storage.googleapis.com/download.tensorflow.org/models/tflite/mobilenet_v1_1.0_224_quant_and_labels.zip"
zip_path = os.path.join(assets_dir, "mobilenet.zip")

print(f"Downloading from {url}...")
urllib.request.urlretrieve(url, zip_path)
print("Downloaded zip.")

with zipfile.ZipFile(zip_path, 'r') as zip_ref:
    zip_ref.extractall(assets_dir)
print("Extracted models.")

# Rename the model to mobilenet_v3.tflite to match our setup
os.rename(os.path.join(assets_dir, "mobilenet_v1_1.0_224_quant.tflite"), os.path.join(assets_dir, "mobilenet_v3.tflite"))

os.remove(zip_path)
print("Cleaned up and finished.")
