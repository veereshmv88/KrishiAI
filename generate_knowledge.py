import json
import os

input_file = "app/src/main/assets/models/disease/class_indices.json"
output_file = "app/src/main/assets/models/disease/disease_knowledge.json"

with open(input_file, 'r') as f:
    indices = json.load(f)

knowledge_base = {}

for key, class_name in indices.items():
    parts = class_name.split("___")
    crop = parts[0].replace("_", " ")
    disease = parts[1].replace("_", " ") if len(parts) > 1 else class_name
    
    is_healthy = "healthy" in disease.lower()
    severity = "Healthy" if is_healthy else "Moderate" # Default to Moderate for diseases

    if not is_healthy:
        if "mildew" in disease.lower() or "spot" in disease.lower():
            severity = "Mild"
        elif "blight" in disease.lower() or "virus" in disease.lower():
            severity = "Severe"

    knowledge_base[class_name] = {
        "cropName": crop,
        "diseaseName": disease,
        "scientificName": f"{crop} {disease} Scientifica" if not is_healthy else "N/A",
        "description": f"This indicates that the {crop} plant is {disease.lower()}." if is_healthy else f"A common disease affecting {crop} plants, characterized by {disease.lower()}.",
        "symptoms": "Normal growth, green leaves, no spots." if is_healthy else f"Visible {disease.lower()} on leaves and stems.",
        "causes": "N/A" if is_healthy else "Fungal/Bacterial pathogens or pests favored by environmental conditions.",
        "spreadMethod": "N/A" if is_healthy else "Wind, water splash, contaminated tools, or insect vectors.",
        "weatherConditions": "All conditions" if is_healthy else "High humidity, warm temperatures, and prolonged wetness.",
        "organicTreatment": "Maintain good agricultural practices." if is_healthy else "Neem oil, copper-based fungicides, or compost tea.",
        "chemicalTreatment": "None required." if is_healthy else "Appropriate commercial fungicides or bactericides as per local guidelines.",
        "recommendedFertilizer": "Balanced NPK fertilizer based on soil test.",
        "recommendedPesticide": "None" if is_healthy else "Consult local agriculture department for approved pesticides.",
        "preventionTips": "Proper crop rotation, spacing, and regular scouting.",
        "irrigationAdvice": "Avoid overhead watering; use drip irrigation.",
        "estimatedRecoveryTime": "N/A" if is_healthy else ("1-2 weeks" if severity == "Mild" else "3-4 weeks"),
        "harvestWarning": "Safe to harvest." if is_healthy else "Observe withholding periods if chemical treatments are applied.",
        "whenToConsultOfficer": "Not necessary." if is_healthy else "If symptoms spread rapidly to more than 20% of the crop.",
        "severityLevel": severity,
        "referenceImagePath": f"assets/images/disease/{class_name}.jpg"
    }

with open(output_file, 'w') as f:
    json.dump(knowledge_base, f, indent=2)

print(f"Generated knowledge base with {len(knowledge_base)} entries.")
