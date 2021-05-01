package com.example.kitbag;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private ImageView imageViewProfile, imageViewSearch;
    private FloatingActionButton fab;

    private String fromDistrict, fromUpazila, toDistrict, toUpazila;

    // Checking list items
    String[] mobileArray = {"Android", "IPhone", "WindowsMobile", "Blackberry",
            "WebOS", "Ubuntu", "Windows7", "Max OS X", "IPhone", "WindowsMobile", "Blackberry",
            "WebOS", "Ubuntu", "Windows7", "Max OS X", "IPhone", "WindowsMobile", "Blackberry",
            "WebOS", "Ubuntu", "Windows7", "Max OS X", "IPhone", "WindowsMobile", "Blackberry",
            "WebOS", "Ubuntu", "Windows7", "Max OS X", "IPhone", "WindowsMobile", "Blackberry",
            "WebOS", "Ubuntu", "Windows7", "Max OS X", "IPhone", "WindowsMobile", "Blackberry",
            "WebOS", "Ubuntu", "Windows7", "Max OS X", "IPhone", "WindowsMobile", "Blackberry",
            "WebOS", "Ubuntu", "Windows7", "Max OS X"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        fab = findViewById(R.id.fab);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        imageViewSearch = findViewById(R.id.imageViewSearch);

        // Checking ListView Items
        ArrayAdapter adapterCheck = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mobileArray);
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapterCheck);

        // Click profile to open drawer
        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.RIGHT);
            }
        });

        // on search button click
        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Inflate Custom layout for searching
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.activity_custom_search_dialog, null);

                // Create Dialog Builder
                AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);

                // Init the editText of the custom dialog box
                AutoCompleteTextView editTextFromDistrict = dialogView.findViewById(R.id.EditTextFromDistrict);
                AutoCompleteTextView editTextFromUpazila = dialogView.findViewById(R.id.EditTextFromUpazila);
                AutoCompleteTextView editTextToDistrict = dialogView.findViewById(R.id.EditTextToDistrict);
                AutoCompleteTextView editTextToUpazila = dialogView.findViewById(R.id.EditTextToUpazila);

                // District Recommendation
                String[] districts = getResources().getStringArray(R.array.Districts);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, districts);
                editTextFromDistrict.setAdapter(adapter);  // District
                editTextToDistrict.setAdapter(adapter);    // District

                // UpazilaFrom Recommendation
                editTextFromDistrict.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String district = adapter.getItem(position);
                        String[] upazilas = null;
                        if (district.equals("Bagerhat")) {
                            upazilas = getResources().getStringArray(R.array.Bagerhat);
                        } else if (district.equals("Bandarban")) {
                            upazilas = getResources().getStringArray(R.array.Bandarban);
                        } else if (district.equals("Barguna")) {
                            upazilas = getResources().getStringArray(R.array.Barguna);
                        } else if (district.equals("Barisal")) {
                            upazilas = getResources().getStringArray(R.array.Barisal);
                        } else if (district.equals("Bhola")) {
                            upazilas = getResources().getStringArray(R.array.Bhola);
                        } else if (district.equals("Bogra")) {
                            upazilas = getResources().getStringArray(R.array.Bogra);
                        } else if (district.equals("Brahmanbaria")) {
                            upazilas = getResources().getStringArray(R.array.Brahmanbaria);
                        } else if (district.equals("Chandpur")) {
                            upazilas = getResources().getStringArray(R.array.Chandpur);
                        } else if (district.equals("Chapainawabganj")) {
                            upazilas = getResources().getStringArray(R.array.Chapainawabganj);
                        } else if (district.equals("Chittagong")) {
                            upazilas = getResources().getStringArray(R.array.Chittagong);
                        } else if (district.equals("Chuadanga")) {
                            upazilas = getResources().getStringArray(R.array.Chuadanga);
                        } else if (district.equals("Cox\\'s Bazar")) {   //dfsgdfgdfgdfgdfgdfgdfgdfgdfgdfg
                            upazilas = getResources().getStringArray(R.array.CoxBazar);
                        } else if (district.equals("Comilla")) {
                            upazilas = getResources().getStringArray(R.array.Comilla);
                        } else if (district.equals("Dhaka")) {
                            upazilas = getResources().getStringArray(R.array.Dhaka);
                        } else if (district.equals("Dinajpur")) {
                            upazilas = getResources().getStringArray(R.array.Dinajpur);
                        } else if (district.equals("Faridpur")) {
                            upazilas = getResources().getStringArray(R.array.Faridpur);
                        } else if (district.equals("Feni")) {
                            upazilas = getResources().getStringArray(R.array.Feni);
                        } else if (district.equals("Gaibandha")) {
                            upazilas = getResources().getStringArray(R.array.Gaibandha);
                        } else if (district.equals("Gazipur")) {
                            upazilas = getResources().getStringArray(R.array.Gazipur);
                        } else if (district.equals("Gopalganj")) {
                            upazilas = getResources().getStringArray(R.array.Gopalganj);
                        } else if (district.equals("Habiganj")) {
                            upazilas = getResources().getStringArray(R.array.Habiganj);
                        } else if (district.equals("Joypurhat")) {
                            upazilas = getResources().getStringArray(R.array.Joypurhat);
                        } else if (district.equals("Jamalpur")) {
                            upazilas = getResources().getStringArray(R.array.Jamalpur);
                        } else if (district.equals("Jessore")) {
                            upazilas = getResources().getStringArray(R.array.Jessore);
                        } else if (district.equals("Jhalokati")) {
                            upazilas = getResources().getStringArray(R.array.Jhalokati);
                        } else if (district.equals("Jhenaidah")) {
                            upazilas = getResources().getStringArray(R.array.Jhenaidah);
                        } else if (district.equals("Khagrachari")) {
                            upazilas = getResources().getStringArray(R.array.Khagrachari);
                        } else if (district.equals("Khulna")) {
                            upazilas = getResources().getStringArray(R.array.Khulna);
                        } else if (district.equals("Kishoreganj")) {
                            upazilas = getResources().getStringArray(R.array.Kishoreganj);
                        } else if (district.equals("Kurigram")) {
                            upazilas = getResources().getStringArray(R.array.Kurigram);
                        } else if (district.equals("Kushtia")) {
                            upazilas = getResources().getStringArray(R.array.Kushtia);
                        } else if (district.equals("Lakshmipur")) {
                            upazilas = getResources().getStringArray(R.array.Lakshmipur);
                        } else if (district.equals("Lalmonirhat")) {
                            upazilas = getResources().getStringArray(R.array.Lalmonirhat);
                        } else if (district.equals("Madaripur")) {
                            upazilas = getResources().getStringArray(R.array.Madaripur);
                        } else if (district.equals("Magura")) {
                            upazilas = getResources().getStringArray(R.array.Magura);
                        } else if (district.equals("Manikganj")) {
                            upazilas = getResources().getStringArray(R.array.Manikganj);
                        } else if (district.equals("Meherpur")) {
                            upazilas = getResources().getStringArray(R.array.Meherpur);
                        } else if (district.equals("Moulvibazar")) {
                            upazilas = getResources().getStringArray(R.array.Moulvibazar);
                        } else if (district.equals("Munshiganj")) {
                            upazilas = getResources().getStringArray(R.array.Munshiganj);
                        } else if (district.equals("Mymensingh")) {
                            upazilas = getResources().getStringArray(R.array.Mymensingh);
                        } else if (district.equals("Naogaon")) {
                            upazilas = getResources().getStringArray(R.array.Naogaon);
                        } else if (district.equals("Narail")) {
                            upazilas = getResources().getStringArray(R.array.Narail);
                        } else if (district.equals("Narayanganj")) {
                            upazilas = getResources().getStringArray(R.array.Narayanganj);
                        } else if (district.equals("Narsingdi")) {
                            upazilas = getResources().getStringArray(R.array.Narsingdi);
                        } else if (district.equals("Natore")) {
                            upazilas = getResources().getStringArray(R.array.Natore);
                        } else if (district.equals("Netrakona")) {
                            upazilas = getResources().getStringArray(R.array.Netrakona);
                        } else if (district.equals("Nilphamari")) {
                            upazilas = getResources().getStringArray(R.array.Nilphamari);
                        } else if (district.equals("Noakhali")) {
                            upazilas = getResources().getStringArray(R.array.Noakhali);
                        } else if (district.equals("Pabna")) {
                            upazilas = getResources().getStringArray(R.array.Pabna);
                        } else if (district.equals("Panchagarh")) {
                            upazilas = getResources().getStringArray(R.array.Panchagarh);
                        } else if (district.equals("Patuakhali")) {
                            upazilas = getResources().getStringArray(R.array.Patuakhali);
                        } else if (district.equals("Pirojpur")) {
                            upazilas = getResources().getStringArray(R.array.Pirojpur);
                        } else if (district.equals("Rajbari")) {
                            upazilas = getResources().getStringArray(R.array.Rajbari);
                        } else if (district.equals("Rajshahi")) {
                            upazilas = getResources().getStringArray(R.array.Rajshahi);
                        } else if (district.equals("Rangpur")) {
                            upazilas = getResources().getStringArray(R.array.Rangpur);
                        } else if (district.equals("Rangamati")) {
                            upazilas = getResources().getStringArray(R.array.Rangamati);
                        } else if (district.equals("Satkhira")) {
                            upazilas = getResources().getStringArray(R.array.Satkhira);
                        } else if (district.equals("Shariatpur")) {
                            upazilas = getResources().getStringArray(R.array.Shariatpur);
                        } else if (district.equals("Sherpur")) {
                            upazilas = getResources().getStringArray(R.array.Sherpur);
                        } else if (district.equals("Sirajganj")) {
                            upazilas = getResources().getStringArray(R.array.Sirajganj);
                        } else if (district.equals("Sunamganj")) {
                            upazilas = getResources().getStringArray(R.array.Sunamganj);
                        } else if (district.equals("Sylhet")) {
                            upazilas = getResources().getStringArray(R.array.Sylhet);
                        } else if (district.equals("Tangail")) {
                            upazilas = getResources().getStringArray(R.array.Tangail);
                        } else if (district.equals("Thakurgaon")) {
                            upazilas = getResources().getStringArray(R.array.Thakurgaon);
                        }
                        if (upazilas != null) {
                            ArrayAdapter<String> adapterUpazila = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, upazilas);
                            editTextFromUpazila.setAdapter(adapterUpazila);  // Define Upazilas
                        }
                    }
                });

                // UpazilaTo Recommendation
                editTextToDistrict.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String district = adapter.getItem(position);
                        String[] upazilas = null;
                        if (district.equals("Bagerhat")) {
                            upazilas = getResources().getStringArray(R.array.Bagerhat);
                        } else if (district.equals("Bandarban")) {
                            upazilas = getResources().getStringArray(R.array.Bandarban);
                        } else if (district.equals("Barguna")) {
                            upazilas = getResources().getStringArray(R.array.Barguna);
                        } else if (district.equals("Barisal")) {
                            upazilas = getResources().getStringArray(R.array.Barisal);
                        } else if (district.equals("Bhola")) {
                            upazilas = getResources().getStringArray(R.array.Bhola);
                        } else if (district.equals("Bogra")) {
                            upazilas = getResources().getStringArray(R.array.Bogra);
                        } else if (district.equals("Brahmanbaria")) {
                            upazilas = getResources().getStringArray(R.array.Brahmanbaria);
                        } else if (district.equals("Chandpur")) {
                            upazilas = getResources().getStringArray(R.array.Chandpur);
                        } else if (district.equals("Chapainawabganj")) {
                            upazilas = getResources().getStringArray(R.array.Chapainawabganj);
                        } else if (district.equals("Chittagong")) {
                            upazilas = getResources().getStringArray(R.array.Chittagong);
                        } else if (district.equals("Chuadanga")) {
                            upazilas = getResources().getStringArray(R.array.Chuadanga);
                        } else if (district.equals("Cox\\'s Bazar")) {   //dfsgdfgdfgdfgdfgdfgdfgdfgdfgdfg
                            upazilas = getResources().getStringArray(R.array.CoxBazar);
                        } else if (district.equals("Comilla")) {
                            upazilas = getResources().getStringArray(R.array.Comilla);
                        } else if (district.equals("Dhaka")) {
                            upazilas = getResources().getStringArray(R.array.Dhaka);
                        } else if (district.equals("Dinajpur")) {
                            upazilas = getResources().getStringArray(R.array.Dinajpur);
                        } else if (district.equals("Faridpur")) {
                            upazilas = getResources().getStringArray(R.array.Faridpur);
                        } else if (district.equals("Feni")) {
                            upazilas = getResources().getStringArray(R.array.Feni);
                        } else if (district.equals("Gaibandha")) {
                            upazilas = getResources().getStringArray(R.array.Gaibandha);
                        } else if (district.equals("Gazipur")) {
                            upazilas = getResources().getStringArray(R.array.Gazipur);
                        } else if (district.equals("Gopalganj")) {
                            upazilas = getResources().getStringArray(R.array.Gopalganj);
                        } else if (district.equals("Habiganj")) {
                            upazilas = getResources().getStringArray(R.array.Habiganj);
                        } else if (district.equals("Joypurhat")) {
                            upazilas = getResources().getStringArray(R.array.Joypurhat);
                        } else if (district.equals("Jamalpur")) {
                            upazilas = getResources().getStringArray(R.array.Jamalpur);
                        } else if (district.equals("Jessore")) {
                            upazilas = getResources().getStringArray(R.array.Jessore);
                        } else if (district.equals("Jhalokati")) {
                            upazilas = getResources().getStringArray(R.array.Jhalokati);
                        } else if (district.equals("Jhenaidah")) {
                            upazilas = getResources().getStringArray(R.array.Jhenaidah);
                        } else if (district.equals("Khagrachari")) {
                            upazilas = getResources().getStringArray(R.array.Khagrachari);
                        } else if (district.equals("Khulna")) {
                            upazilas = getResources().getStringArray(R.array.Khulna);
                        } else if (district.equals("Kishoreganj")) {
                            upazilas = getResources().getStringArray(R.array.Kishoreganj);
                        } else if (district.equals("Kurigram")) {
                            upazilas = getResources().getStringArray(R.array.Kurigram);
                        } else if (district.equals("Kushtia")) {
                            upazilas = getResources().getStringArray(R.array.Kushtia);
                        } else if (district.equals("Lakshmipur")) {
                            upazilas = getResources().getStringArray(R.array.Lakshmipur);
                        } else if (district.equals("Lalmonirhat")) {
                            upazilas = getResources().getStringArray(R.array.Lalmonirhat);
                        } else if (district.equals("Madaripur")) {
                            upazilas = getResources().getStringArray(R.array.Madaripur);
                        } else if (district.equals("Magura")) {
                            upazilas = getResources().getStringArray(R.array.Magura);
                        } else if (district.equals("Manikganj")) {
                            upazilas = getResources().getStringArray(R.array.Manikganj);
                        } else if (district.equals("Meherpur")) {
                            upazilas = getResources().getStringArray(R.array.Meherpur);
                        } else if (district.equals("Moulvibazar")) {
                            upazilas = getResources().getStringArray(R.array.Moulvibazar);
                        } else if (district.equals("Munshiganj")) {
                            upazilas = getResources().getStringArray(R.array.Munshiganj);
                        } else if (district.equals("Mymensingh")) {
                            upazilas = getResources().getStringArray(R.array.Mymensingh);
                        } else if (district.equals("Naogaon")) {
                            upazilas = getResources().getStringArray(R.array.Naogaon);
                        } else if (district.equals("Narail")) {
                            upazilas = getResources().getStringArray(R.array.Narail);
                        } else if (district.equals("Narayanganj")) {
                            upazilas = getResources().getStringArray(R.array.Narayanganj);
                        } else if (district.equals("Narsingdi")) {
                            upazilas = getResources().getStringArray(R.array.Narsingdi);
                        } else if (district.equals("Natore")) {
                            upazilas = getResources().getStringArray(R.array.Natore);
                        } else if (district.equals("Netrakona")) {
                            upazilas = getResources().getStringArray(R.array.Netrakona);
                        } else if (district.equals("Nilphamari")) {
                            upazilas = getResources().getStringArray(R.array.Nilphamari);
                        } else if (district.equals("Noakhali")) {
                            upazilas = getResources().getStringArray(R.array.Noakhali);
                        } else if (district.equals("Pabna")) {
                            upazilas = getResources().getStringArray(R.array.Pabna);
                        } else if (district.equals("Panchagarh")) {
                            upazilas = getResources().getStringArray(R.array.Panchagarh);
                        } else if (district.equals("Patuakhali")) {
                            upazilas = getResources().getStringArray(R.array.Patuakhali);
                        } else if (district.equals("Pirojpur")) {
                            upazilas = getResources().getStringArray(R.array.Pirojpur);
                        } else if (district.equals("Rajbari")) {
                            upazilas = getResources().getStringArray(R.array.Rajbari);
                        } else if (district.equals("Rajshahi")) {
                            upazilas = getResources().getStringArray(R.array.Rajshahi);
                        } else if (district.equals("Rangpur")) {
                            upazilas = getResources().getStringArray(R.array.Rangpur);
                        } else if (district.equals("Rangamati")) {
                            upazilas = getResources().getStringArray(R.array.Rangamati);
                        } else if (district.equals("Satkhira")) {
                            upazilas = getResources().getStringArray(R.array.Satkhira);
                        } else if (district.equals("Shariatpur")) {
                            upazilas = getResources().getStringArray(R.array.Shariatpur);
                        } else if (district.equals("Sherpur")) {
                            upazilas = getResources().getStringArray(R.array.Sherpur);
                        } else if (district.equals("Sirajganj")) {
                            upazilas = getResources().getStringArray(R.array.Sirajganj);
                        } else if (district.equals("Sunamganj")) {
                            upazilas = getResources().getStringArray(R.array.Sunamganj);
                        } else if (district.equals("Sylhet")) {
                            upazilas = getResources().getStringArray(R.array.Sylhet);
                        } else if (district.equals("Tangail")) {
                            upazilas = getResources().getStringArray(R.array.Tangail);
                        } else if (district.equals("Thakurgaon")) {
                            upazilas = getResources().getStringArray(R.array.Thakurgaon);
                        }
                        if (upazilas != null) {
                            ArrayAdapter<String> adapterUpazila = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, upazilas);
                            editTextToUpazila.setAdapter(adapterUpazila);  // Define Upazilas
                        }
                    }
                });

                //Setting positive "Ok" Button
                ab.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here...
                    }
                });
                //Setting Negative "Cancel" Button
                ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                    }
                });
                ab.setCancelable(false);
                ab.setView(dialogView);
                ab.show();
            }
        });

        // Show or Hide Floating Action Button
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                fab.hide();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                fab.show();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

    }
}