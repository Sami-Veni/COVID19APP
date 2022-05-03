package com.samiamare.covid_19.ui.country;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.samiamare.covid_19.R;

public class CovidCountryDetail extends AppCompatActivity {

    TextView tvDetailCountryName, tvDetailTotalCases, tvDetailTodayCases, tvDetailTotalDeaths,
            tvDetailTodayDeaths, tvDetailTotalRecovered, tvDetailTotalActive, tvDetailTotalCritical;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covid_country_detail);

        // call view
        tvDetailCountryName = findViewById(R.id.tvDetailCountryName);
        tvDetailTotalCases = findViewById(R.id.tvDetailTotalCases);
        tvDetailTodayCases = findViewById(R.id.tvDetailTodayCases);
        tvDetailTotalDeaths = findViewById(R.id.tvDetailTotalDeaths);
        tvDetailTodayDeaths = findViewById(R.id.tvDetailTodayDeaths);
        tvDetailTotalRecovered = findViewById(R.id.tvDetailTotalRecovered);
        tvDetailTotalActive = findViewById(R.id.tvDetailTotalActive);
        tvDetailTotalCritical = findViewById(R.id.tvDetailTotalCritical);


        // call Covid Country
        CovidCountry covidCountry = getIntent().getParcelableExtra("EXTRA_COVID");

        // set text view
        tvDetailCountryName.setText(checkZero(covidCountry.getmCovidCountry()));
        tvDetailTotalCases.setText(checkZero(Integer.toString(covidCountry.getmCases())));
        tvDetailTodayCases.setText(checkZero(covidCountry.getmTodayCases()));
        tvDetailTotalDeaths.setText(checkZero(covidCountry.getmDeaths()));
        tvDetailTodayDeaths.setText(checkZero(covidCountry.getmTodayDeaths()));
        tvDetailTotalRecovered.setText(checkZero(covidCountry.getmRecovered()));
        tvDetailTotalActive.setText(checkZero(covidCountry.getmActive()));
        tvDetailTotalCritical.setText(checkZero(covidCountry.getmCritical()));

    }
    public String checkZero(String stat){
        if (stat.equals("0"))
            return "---";
        else
            return stat;
    }
}
