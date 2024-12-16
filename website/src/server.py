from flask import Flask, render_template, request
import os

app = Flask(__name__, template_folder="./templates")

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/smartIrrigation')
def smart_irrigation():
    print("Serving smartIrrigation.html")
    return render_template('smartIrrigation.html')

@app.route('/calculate', methods=['POST'])
def calculate():
    try:
        # Get user inputs from form
        current_emissions = float(request.form['current_emissions'])
        renewable_percentage = float(request.form['renewable_percentage'])

        # Calculate carbon footprints
        baseline_footprint = current_emissions
        reduction_factor = renewable_percentage / 100
        suggested_footprint = current_emissions * (1 - reduction_factor)

        return render_template('index.html', 
                               baseline_footprint=baseline_footprint,
                               suggested_footprint=suggested_footprint)
    except ValueError:
        return render_template('index.html', error="Please enter valid numeric values.")

if __name__ == '__main__':
    app.run(debug=True)