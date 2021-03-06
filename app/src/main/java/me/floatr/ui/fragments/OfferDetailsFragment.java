package me.floatr.ui.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.floatr.R;
import me.floatr.models.Initiate;
import me.floatr.models.Loan;
import me.floatr.models.LoanOffer;
import me.floatr.ui.activities.MainActivity;
import me.floatr.util.PreferenceNames;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jason on 10/29/16.
 */

public class OfferDetailsFragment extends Fragment implements View.OnClickListener {

    private View view;
    private MainActivity mainActivity;
    private SharedPreferences mainPref;

    @BindView(R.id.offerDetailFragMaxRange)
    public View offerDetailFragMaxRange;
    @BindView(R.id.offerDetailFragMinRange)
    public View offerDetailFragMinRange;

    @BindView(R.id.offerDetailFragNameText)
    public TextView offerDetailFragNameText;

    @BindView(R.id.offerDetailBankSpinner)
    public Spinner bankDropdown;

    @BindView(R.id.offerDetailFragMinRangeValue)
    public TextView offerDetailFragMinRangeValue;

    @BindView(R.id.offerDetailFragMaxRangeValue)
    public TextView offerDetailFragMaxRangeValue;

    @BindView(R.id.offerDetailFragInterestRateValue)
    public TextView offerDetailFragInterestRateValue;

    @BindView(R.id.offerDetailFragPeriodValue)
    public TextView offerDetailFragPeriodValue;

    @BindView(R.id.offerDetailFragPeriodUnit)
    public TextView offerDetailFragPeriodUnit;

    @BindView(R.id.seekBar)
    public SeekBar seekBar;

    @BindView(R.id.sliderMaxRange)
    public TextView sliderMaxRange;

    @BindView(R.id.sliderMinRange)
    public TextView sliderMinRange;

    @BindView(R.id.sliderText)
    public TextView sliderText;

    @BindView(R.id.initiateButton)
    public Button initiateButton;

    String offerId;
    LoanOffer offer;

    String firstname;
    String lastName;
    Integer initiate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        mainPref = mainActivity.getSharedPreferences(PreferenceNames.MAIN_PREFS_NAME, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.offerdetails_fragment,
                container, false);
        ButterKnife.bind(this, view);
        mainActivity.getSupportActionBar().setTitle("Offer Details");

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.container, new LoanOffersFragment()).commit();
                    return true;
                }
                return false;
            }
        });

        mainActivity.getSupportActionBar().setTitle("Loan Offer");

        offerId = getArguments().getString("id");
        firstname = getArguments().getString("firstname");
        lastName = getArguments().getString("lastname");
        initiate = getArguments().getInt("initiateValue");

        Set<String> bankAccounts = mainPref.getStringSet(PreferenceNames.PREF_USER_BANK_ACCOUNTS, new HashSet<String>());
        String[] bankAccountsArr = bankAccounts.toArray(new String[bankAccounts.size()]);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, bankAccountsArr);
        bankDropdown.setAdapter(adapter2);


        Call<LoanOffer> offerCall = mainActivity.apiService.getLoanOffer(offerId);
        offerCall.enqueue(new Callback<LoanOffer>() {
            @Override
            public void onResponse(Call<LoanOffer> call, Response<LoanOffer> response) {
                offer = response.body();
                offerDetailFragMinRangeValue.setText("" + offer.getMinOffer());
                offerDetailFragMaxRangeValue.setText("" + offer.getMaxOffer());
                offerDetailFragInterestRateValue.setText("" + offer.getInterestRate());
                offerDetailFragPeriodValue.setText("" + offer.getPeriod());
                offerDetailFragPeriodUnit.setText("" + offer.getPeriodUnit());
                offerDetailFragNameText.setText("" + offer.getLoaner().getFirstName() + " "
                        + offer.getLoaner().getLastName());

                sliderMaxRange.setText(offer.getMaxOffer() + "");
                sliderMinRange.setText(offer.getMinOffer() + "");
                sliderText.setText(offer.getMinOffer() + "");


                if (offer.getStatus().equals("initiated")) {
                    offerDetailFragNameText.setText(firstname + " " + lastName);
                    sliderMinRange.setVisibility(View.INVISIBLE);
                    sliderMaxRange.setVisibility(View.INVISIBLE);
                    offerDetailFragMaxRange.setVisibility(View.GONE);
                    offerDetailFragMinRange.setVisibility(View.GONE);
                    double progress = ((double) (initiate - offer.getMinOffer())) / (offer.getMaxOffer() - offer.getMinOffer()) * 100;
                    seekBar.setProgress((int) progress);
                    seekBar.setEnabled(false);
                    initiateButton.setText("Confirm Request?");
                }

            }

            @Override
            public void onFailure(Call<LoanOffer> call, Throwable t) {

            }
        });


        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double percent = (double) progress / 100;
                int min = offer.getMinOffer();
                int max = offer.getMaxOffer();
                double prog = min + (percent * (max - min));
                sliderText.setText(Math.round(prog) + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        initiateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (offer.getStatus().equals("initiated")) {
                    mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.container, new YourOffersFragment()).commit();
                    return;
                }

                else if (offer.getLoaner().getId().equals(mainPref.getString(PreferenceNames.PREF_USER_ID, ""))) {
                    Toast.makeText(getContext(), "Can't request, this is your own offer!", Toast.LENGTH_LONG).show();
                    return;
                }


                Call<Loan> loanCall = mainActivity.apiService.initiate(offer.getId(), new Initiate(
                        Integer.parseInt(sliderText.getText().toString()), (String) bankDropdown.getSelectedItem()));
                loanCall.enqueue(new Callback<Loan>() {
                    @Override
                    public void onResponse(Call<Loan> call, Response<Loan> response) {

                    }

                    @Override
                    public void onFailure(Call<Loan> call, Throwable t) {

                    }
                });
                mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.container, new LoanOffersFragment()).commit();

            }
        });


        this.view = view;
        return view;
    }


    @Override
    public void onClick(View v) {
//        if (v == createOfferFragCreateButton) {
//
//        }
    }
}