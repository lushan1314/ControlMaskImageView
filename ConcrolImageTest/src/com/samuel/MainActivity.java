
package com.samuel;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;



public class MainActivity extends Activity {


    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
    }

 

}
