from flask import Flask, render_template, request

app = Flask(__name__, template_folder="templates", static_folder="static")

@app.route('/')
def index():
    return render_template('index.html')  # Route for index.html

@app.route('/smartIrrigation')
def smart_irrigation():
    return render_template('smartIrrigation.html')  # Route for smartIrrigation.html

@app.route('/calculate', methods=['POST'])
def calculate():
    try:
        # Retrieve form data
        ghg_type = request.form['ghg_type']
        current_emissions = float(request.form['current_emissions'])
        renewable_percentage = float(request.form['renewable_percentage'])

        # Define GWP values for all GHGs
        gwp_values = {
            'CO2': 1,
            'CH4': 28,
            'N2O': 273,
            'SF6': 22800,
            'HFC-134a': 1430,
            'PFC': 7390,
            'NF3': 17200
        }

        # Conversion to CO2-equivalents
        gwp = gwp_values.get(ghg_type, 1)
        co2_equivalent_emissions = current_emissions * gwp

        # Calculate baseline and suggested footprints
        baseline_footprint = co2_equivalent_emissions
        reduction_factor = renewable_percentage / 100
        suggested_footprint = co2_equivalent_emissions * (1 - reduction_factor)

        # Pass results to the template
        return render_template(
            'index.html', 
            baseline_footprint=baseline_footprint,
            suggested_footprint=suggested_footprint
        )
    except ValueError:
        return render_template('index.html', error="Please enter valid numeric values.")

if __name__ == '__main__':
    app.run(debug=True)