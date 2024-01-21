


#load the model
print("Loading models...")
model_incoming_bikes = load(r'BugBusters\data_training\random_forest_model_incoming.joblib')
model_outgoing_bikes = load(r'BugBusters\data_training\random_forest_model_outgoing.joblib')
print("Models loaded!")


#load json files
with open('BugBusters\data_training\id_to_station.json', 'r') as fp:
    station_to_id = json.load(fp)
