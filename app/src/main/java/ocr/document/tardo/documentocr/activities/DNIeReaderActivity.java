/* Copyright 2019  Alexandre Díaz - <dev@redneboa.es>
 * Original code from https://www.dnielectronico.es/descargas/Apps/Android_DGPApp_LECTURA.rar
 *
 * License GPL-3.0 or later (http://www.gnu.org/licenses/gpl.html).
 */
package ocr.document.tardo.documentocr.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import ocr.document.tardo.documentocr.AppMain;
import ocr.document.tardo.documentocr.R;
import ocr.document.tardo.documentocr.components.NFCOperationsEnc;

public class DNIeReaderActivity extends Activity {

    public static final String ACTION_READ = "ACTION_READ";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);  
        setContentView(R.layout.activity_dnie_reader);
    	    	    	    	             
        // If not open properly, go out
        if (!((AppMain)getApplicationContext()).isStarted())
        {
        	PackageManager packman = getApplicationContext().getPackageManager();
        	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
        	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
      
        	android.os.Process.killProcess(android.os.Process.myPid());
	     	System.exit(0);
	     	
        	return;
        }

    	PackageManager packman = getApplicationContext().getPackageManager();
    	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
    	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

		final Button btnBack = (Button)findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) {
			onBackPressed();
			finish();
			}
		});
     }
    
    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

    	PackageManager packman = getApplicationContext().getPackageManager();
    	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
    	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	}
    
    @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

    	PackageManager packman = getApplicationContext().getPackageManager();
    	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
    	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	}
    
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

    	PackageManager packman = getApplicationContext().getPackageManager();
    	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
    	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	}
}