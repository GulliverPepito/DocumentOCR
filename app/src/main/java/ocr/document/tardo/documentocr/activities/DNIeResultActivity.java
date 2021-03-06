/* Copyright 2019  Alexandre Díaz - <dev@redneboa.es>
 * Original code from https://www.dnielectronico.es/descargas/Apps/Android_DGPApp_LECTURA.rar
 *
 * License GPL-3.0 or later (http://www.gnu.org/licenses/gpl.html).
 */
package ocr.document.tardo.documentocr.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eiqui.odoojson_rpc.JSONRPCClientOdoo;
import com.eiqui.odoojson_rpc.exceptions.OdooSearchException;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.tsenger.androsmex.mrtd.DG11;
import de.tsenger.androsmex.mrtd.DG1_Dnie;
import de.tsenger.androsmex.mrtd.DG2;
import de.tsenger.androsmex.mrtd.DG7;
import ocr.document.tardo.documentocr.AppMain;
import ocr.document.tardo.documentocr.R;
import ocr.document.tardo.documentocr.utils.Constants;
import ocr.document.tardo.documentocr.utils.DateHelper;
import ocr.document.tardo.documentocr.utils.jj2000.J2kStreamDecoder;

public class DNIeResultActivity extends Activity implements View.OnClickListener {

    public DG1_Dnie mDG1;
    public DG11 mDG11;
    private DG2 mDG2;
    private DG7 mDG7;

    public Bitmap mLoadedImage;
    private Bitmap mLoadedSignature;

    private Button mButtonBack;
    private Button mButtonStartRead;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dnie_result);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            byte [] dataDG1	= extras.getByteArray("DGP_DG1");
            byte [] dataDG2	= extras.getByteArray("DGP_DG2");
            byte [] dataDG7	= extras.getByteArray("DGP_DG7");
            byte [] dataDG11 	= extras.getByteArray("DGP_DG11");

            if (dataDG1!=null) mDG1 = new DG1_Dnie(dataDG1);
            if (dataDG2!=null) mDG2 = new DG2(dataDG2);
            if (dataDG7!=null) mDG7 = new DG7(dataDG7);
            if (dataDG11!=null) mDG11 = new DG11(dataDG11);

            TextView tvloc;
            if (mDG1 != null) {
                // Name
                tvloc = findViewById(R.id.CITIZEN_data_tab_01);
                tvloc.setText(mDG1.getName());
                // Surname
                tvloc = findViewById(R.id.CITIZEN_data_tab_02);
                tvloc.setText(mDG1.getSurname());
                // Doc. Number
                tvloc = findViewById(R.id.CITIZEN_data_tab_03);
                tvloc.setText(mDG1.getDocNumber());
                // Date of Expiry
                tvloc = findViewById(R.id.CITIZEN_data_tab_03_caducity);
                tvloc.setText(mDG1.getDateOfExpiry());
                // Expedition
                tvloc = findViewById(R.id.CITIZEN_data_tab_10);
                try {
                    DateFormat dtFormat = DateFormat.getDateInstance(2);
                    Date expiryDate = dtFormat.parse(mDG1.getDateOfExpiry());
                    Date birthday = dtFormat.parse(mDG1.getDateOfBirth());
                    Date dnieTest = DateHelper.getExpeditionDate(birthday, expiryDate);
                    String strDate = dtFormat.format(dnieTest);
                    tvloc.setText(strDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // Birthday
                tvloc = findViewById(R.id.CITIZEN_data_tab_07);
                // Nationality
                tvloc.setText(mDG1.getDateOfBirth());
                tvloc = findViewById(R.id.CITIZEN_data_tab_09);
                tvloc.setText(mDG1.getNationality().toUpperCase());
                // Gender
                tvloc = findViewById(R.id.CITIZEN_data_tab_gender);
                tvloc.setText(mDG1.getSex());
            }

            if (mDG11 != null) {
                // Birth Place
                tvloc = findViewById(R.id.CITIZEN_data_tab_08);
                tvloc.setText(mDG11.getBirthPlace().replace("<", " (") + ")");
                // DNIe Number
                tvloc = findViewById(R.id.CITIZEN_data_tab_03);
                tvloc.setText(mDG11.getPersonalNumber());
                try {
                    String[] address = mDG11.getAddress(0).split("<");
                    // Address
                    tvloc = findViewById(R.id.CITIZEN_data_tab_04);
                    tvloc.setText(address[0]);

                    // Province
                    if (address.length >= 3) {
                        tvloc = findViewById(R.id.CITIZEN_data_tab_05);
                        //tvloc.setText(mDG11.getAddress(DG11.ADDR_PROVINCIA));
                        tvloc.setText(address[2]);
                    }
                    // Location
                    tvloc = findViewById(R.id.CITIZEN_data_tab_06);
                    tvloc.setText(address[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }

            // Photo
            ImageView ivFoto = findViewById(R.id.CITIZEN_data_tab_00);
            if (dataDG2 != null) {
                try {
                    // JPEG-2000 Parse
                    byte [] imagen = mDG2.getImageBytes();
                    J2kStreamDecoder j2k = new J2kStreamDecoder();
                    ByteArrayInputStream bis = new ByteArrayInputStream(imagen);
                    mLoadedImage = j2k.decode(bis);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            if (mLoadedImage != null)
                ivFoto.setImageBitmap(mLoadedImage);
            else
                ivFoto.setImageResource(R.drawable.noface);

            // Signature
            ImageView ivFirma = findViewById(R.id.CITIZEN_data_tab_00_SIGNATURE);
            if (dataDG7 != null) {
                try {
                    // JPEG-2000 Parse
                    byte [] imagen = mDG7.getImageBytes();
                    J2kStreamDecoder j2k = new J2kStreamDecoder();
                    ByteArrayInputStream bis = new ByteArrayInputStream(imagen);
                    mLoadedSignature = j2k.decode(bis);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                if (mLoadedSignature != null) {
                    ivFirma.setVisibility(ImageView.VISIBLE);
                    ivFirma.setImageBitmap(mLoadedSignature);
                }
            }
        }

        mButtonBack = findViewById(R.id.btnBack);
        mButtonStartRead = findViewById(R.id.btnValidate);

        mButtonBack.setOnClickListener(this);
        mButtonStartRead.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnBack) {
            Intent intent = new Intent(DNIeResultActivity.this, ReadModeActivity.class);
            startActivity(intent);
            finish();
        }
        else if (v.getId() == R.id.btnValidate) {
            final Button btnValidate = (Button)v;
            btnValidate.setEnabled(false);
            btnValidate.setText("Sending...");
            mBackgroundHandler.post(new RPCCreatePartner(this, ((AppMain)getApplication()).OdooClient()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    @Override
    public void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("RPCBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showToast(final String text) {
        final Activity activity = this;
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    private static class RPCCreatePartner implements Runnable {

        final JSONRPCClientOdoo mClient;
        final private DNIeResultActivity ocrbResultActivity;
        private int mOperationResult;


        RPCCreatePartner(DNIeResultActivity activity, JSONRPCClientOdoo client) {
            mClient = client;
            ocrbResultActivity = activity;
        }


        @Override
        public void run() {
            final SharedPreferences Settings = ocrbResultActivity.getSharedPreferences(Constants.SHARED_PREFS_USER_INFO, Context.MODE_PRIVATE);
            final Boolean hasHotelL10N = Settings.getBoolean("HasHotelL10N", false);
            String name = ocrbResultActivity.mDG1.getSurname() + "  " + ocrbResultActivity.mDG1.getName();
            String docNumber = ocrbResultActivity.mDG11.getPersonalNumber();
            String gender = ocrbResultActivity.mDG1.getSex();
            String nation = ocrbResultActivity.mDG1.getNationality();
            Date expiryDate = null;
            Date birthday = null;
            try {
                DateFormat dtDNIFormat = DateFormat.getDateInstance(2);
                expiryDate = dtDNIFormat.parse(ocrbResultActivity.mDG1.getDateOfExpiry());
                birthday = dtDNIFormat.parse(ocrbResultActivity.mDG1.getDateOfBirth());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            DateFormat dtFormat = new SimpleDateFormat("YYYY-MM-dd");
            Date dnieTest = DateHelper.getExpeditionDate(birthday, expiryDate);
            String strExpDate = dtFormat.format(dnieTest);
            String strBirthDate = dtFormat.format(birthday);

            try {
                Integer codeIneId = 0;
                String[] address = ocrbResultActivity.mDG11.getAddress(0).split("<");
                if (hasHotelL10N && address.length == 3) {
                    JSONArray searchResult = mClient.callSearch("code.ine", String.format("[['name', '=ilike', '%s%c']]", ocrbResultActivity.mDG11.getAddress(0).split("<")[2], '%'), "['id', 'code', 'display_name']");
                    if (null != searchResult) {
                        codeIneId = searchResult.getJSONObject(0).getInt("id");
                    }
                }


                ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
                ocrbResultActivity.mLoadedImage.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOS);
                String encodedPhoto = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.NO_WRAP);

                String createValues;
                // Hotel l10 Support
                if (hasHotelL10N) {
                    createValues = String.format(
                            "{'name': '%s', 'image': '%s', 'document_number': '%s', 'birthdate_date': '%s', 'gender': '%s', 'document_expedition_date': '%s', 'code_ine_id': %d, comment: 'Nation: %s'}",
                            name, encodedPhoto, docNumber, strBirthDate, gender, strExpDate, codeIneId, nation);
                } else {
                    createValues = String.format(
                            "{'image': '%s', 'name': '%s', 'vat': '%s', comment: 'Birthday: %s\nGender: %s\nNation: %s\nDocument Expedition Date: %s'}",
                            name, docNumber, strBirthDate, gender, nation, strExpDate);
                }
                mOperationResult = mClient.callCreate("res.partner", createValues);

                if (mOperationResult != JSONRPCClientOdoo.ERROR) {
                    ocrbResultActivity.showToast(ocrbResultActivity.getApplicationContext().getString(R.string.jsonrpc_partner_created));
                    Intent intent = new Intent(ocrbResultActivity, ReadModeActivity.class);
                    ocrbResultActivity.startActivity(intent);
                    ocrbResultActivity.finish();
                } else {

                    ocrbResultActivity.showToast(ocrbResultActivity.getApplicationContext().getString(R.string.jsonrpc_partner_error));
                    final Button btnValidate = ocrbResultActivity.findViewById(R.id.btnValidate);
                    btnValidate.setText(R.string.validate);
                    btnValidate.setEnabled(true);
                }
            } catch (OdooSearchException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
