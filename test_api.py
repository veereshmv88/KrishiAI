import urllib.request
import json

url = "https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070?api-key=579b464db66ec23bdd000001b3352ede1d0f45386f64526e50d45c3f&format=json&limit=5"
try:
    with urllib.request.urlopen(url) as response:
        data = json.loads(response.read().decode())
        if 'records' in data:
            print("SUCCESS! Found {} records.".format(len(data['records'])))
            for record in data['records']:
                print(f" - {record.get('commodity')} in {record.get('state')} / {record.get('district')}: Min {record.get('min_price')}, Max {record.get('max_price')}")
        else:
            print("API returned data but no 'records' field:", data)
except Exception as e:
    print("Error calling API:", e)
