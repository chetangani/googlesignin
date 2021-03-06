package in.askdial.testgooglesheets;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final int RC_SIGN_IN = 007;
    private GoogleApiClient mGoogleApiClient;
    SignInButton signInButton;
    Button signoutbtn;
    TextView tvnametxt, tvname, tvemailtxt, tvemail;
    ImageView profileimage;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    String Name="", Email="", Image="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signoutbtn = (Button) findViewById(R.id.signout);

        tvnametxt = (TextView) findViewById(R.id.nametxt);
        tvname = (TextView) findViewById(R.id.name);
        tvemailtxt = (TextView) findViewById(R.id.emailtxt);
        tvemail = (TextView) findViewById(R.id.email);

        profileimage = (ImageView) findViewById(R.id.imageView);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = settings.edit();

        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());

        signInButton.setOnClickListener(this);
        signoutbtn.setOnClickListener(this);

        boolean result = settings.getBoolean("signin", false);
        Log.d("debug", "Result: "+result);

        if (result == false) {
            updateUI(false, settings.getString("Name", ""), settings.getString("email", ""), settings.getString("profileimage", ""));
        } else {
            updateUI(true, settings.getString("Name", ""), settings.getString("email", ""), settings.getString("profileimage", ""));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;

            case R.id.signout:
                signOut();
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("debug", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Name = acct.getDisplayName();
            editor.putString("Name", Name);
            editor.putString("email", acct.getEmail());
            Image = acct.getPhotoUrl().toString();
            editor.putString("profileimage", Image);
            editor.putBoolean("signin", true);
            editor.commit();
            updateUI(true, settings.getString("Name", ""), settings.getString("email", ""), settings.getString("profileimage", ""));
        } else {
            // Signed out, show unauthenticated UI.
            editor.putString("Name", "");
            editor.putString("email", "");
            editor.putString("profileimage", "");
            editor.putBoolean("signin", false);
            editor.commit();
            updateUI(false, settings.getString("Name", ""), settings.getString("email", ""), settings.getString("profileimage", ""));
        }
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        editor.putString("Name", "");
                        editor.putString("email", "");
                        editor.putString("profileimage", "");
                        editor.putBoolean("signin", false);
                        editor.commit();
                        updateUI(false, settings.getString("Name", ""), settings.getString("email", ""),
                                settings.getString("profileimage", ""));
                    }
                });
    }

    private void updateUI(boolean value, String name, String email, String image) {
        if (value) {
            tvname.setText(name);
            tvemail.setText(email);
            Picasso.with(MainActivity.this).load(image).into(profileimage);
            profileimage.setVisibility(View.VISIBLE);
            tvnametxt.setVisibility(View.VISIBLE);
            tvname.setVisibility(View.VISIBLE);
            tvemailtxt.setVisibility(View.VISIBLE);
            tvemail.setVisibility(View.VISIBLE);
            signoutbtn.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.GONE);
        } else {
            profileimage.setVisibility(View.GONE);
            tvnametxt.setVisibility(View.GONE);
            tvname.setVisibility(View.GONE);
            tvemailtxt.setVisibility(View.GONE);
            tvemail.setVisibility(View.GONE);
            signoutbtn.setVisibility(View.GONE);
            signInButton.setVisibility(View.VISIBLE);
        }
    }
}
