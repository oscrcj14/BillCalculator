package com.rabbidgames.groupon;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.flurry.android.FlurryAgent;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.*;

public class Groupon extends Activity
 					implements URLCaller {
	final static String SYSTEM_MESSAGE_TIMESTAMP_KEY = "GrouponSystemMessagesTimestamp";
	final static String SYSTEM_MESSAGE_CACHED_KEY = "GrouponSystemMessageCached";
	
    enum Fields {None, All, GrouponCost, GrouponValue, SizeOfGroup, TaxRate, TipAmount};
	Resources m_Res;
	
    // UI Members
	LinearLayout m_MainLinearLayout;
	List<Person> m_PersonLayouts = new ArrayList<Person>();
	TextView m_DisclaimerText;
	EditText m_GrouponCostText;
	TextView m_GrouponCostLabel;
	EditText m_GrouponValueText;
	TextView m_GrouponValueLabel;
	EditText m_TaxRateText;
	TextView m_TaxRateLabel;
	EditText m_TipAmountText;
	TextView m_TipAmountLabel;
	EditText m_SizeOfGroupText;
	TextView m_SizeOfGroupLabel;
	Button m_CalculateButton;
	
	//Data
	public class PageData
	{
		float m_GrouponCost = 0f;
		float m_GrouponValue = 0f;
		int m_SizeOfGroup = 0;
		float m_TaxRate = 0f;
		float m_TipAmount = 0f;
		List<Float> m_FoodCosts = new ArrayList<Float>();
	}
	
	PageData m_Data = new PageData();
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		m_Res = this.getBaseContext().getResources();
        setContentView(R.layout.main);
        
        PopulateIDs();
        SetupListeners();
        
        m_CalculateButton.requestFocus();
       
        TryRecallData();
        
        StaticObjects.ShowAd(this, true);
        
        if(ShouldCallForSystemMessage())
        {
        	LoadSystemMessages();
        }
        
        UpdateSystemMessageFromCache();
    }
    
    @Override
    public void onStart()
    {
       super.onStart();
       FlurryAgent.onStartSession(this, StaticObjects.FlurryKey());
    }
    
    @Override
    public void onStop()
    {
       super.onStop();
       FlurryAgent.onEndSession(this);
    }
    
    @Override
    public Object onRetainNonConfigurationInstance()
    {
    	final PageData saveData = m_Data;
    	return saveData;
    }
    
    private void TryRecallData()
    {
        final Object data = getLastNonConfigurationInstance();

        if(data != null)
        {  
        	m_Data = (PageData)data;
        	RefreshMainFields();
        	RefreshPersonLayouts(true);
        }
        
        Calculate(Fields.All);
    }
    
    private void PopulateIDs()
    {
    	m_MainLinearLayout = (LinearLayout)this.findViewById(R.id.MainLinearLayout);
    	//m_SystemMessagesList = (ListView)this.findViewById(R.id.SystemMessagesListView);
    	m_DisclaimerText = (TextView)this.findViewById(R.id.DisclaimerText);
    	m_SizeOfGroupText = (EditText)this.findViewById(R.id.PartyNumberText);
        m_SizeOfGroupLabel = (TextView)this.findViewById(R.id.PartyNumberLabel);
        m_GrouponCostText = (EditText)this.findViewById(R.id.GrouponCostText);
        m_GrouponCostLabel = (TextView)this.findViewById(R.id.GrouponCostLabel);
        m_GrouponValueText = (EditText)this.findViewById(R.id.GrouponValueText);
        m_GrouponValueLabel = (TextView)this.findViewById(R.id.GrouponValueLabel);
        m_TaxRateText = (EditText)this.findViewById(R.id.TaxRateText);
        m_TaxRateLabel = (TextView)this.findViewById(R.id.TaxRateLabel);
        m_TipAmountText = (EditText)this.findViewById(R.id.TipAmountText);
        m_TipAmountLabel = (TextView)this.findViewById(R.id.TipAmountLabel);
        m_CalculateButton = (Button)this.findViewById(R.id.CalculateButton);
    }
    
    private void SetupListeners()
    {
        m_CalculateButton.setOnClickListener(new OnClickListener() {
	          @Override
	          public void onClick(View v) {
	        	  m_CalculateButton.requestFocus();
	        	  RefreshPersonLayouts(false);
	        	  Calculate(Fields.All);
	          }
	        });
        
    	m_GrouponCostText.setOnFocusChangeListener(new OnFocusChangeListener() {
	          @Override
	          public void onFocusChange(View view, boolean hasFocus) {
	        	  if(!hasFocus)
	        	  {
	        		  Calculate(Fields.GrouponCost);
	        	  }
	          }
	    });
      
	      m_GrouponValueText.setOnFocusChangeListener(new OnFocusChangeListener() {
		          @Override
		          public void onFocusChange(View view, boolean hasFocus) {
		        	  if(!hasFocus)
		        	  {
		        		  Calculate(Fields.GrouponValue);
		        	  }
		          }
		    });
	      
	      m_SizeOfGroupText.setOnFocusChangeListener(new OnFocusChangeListener() {
		          @Override
		          public void onFocusChange(View view, boolean hasFocus) {
		        	  if(!hasFocus)
		        	  {
		        		  RefreshPersonLayouts(false);
		        	  }
		          }
		    });
	      
	      m_TaxRateText.setOnFocusChangeListener(new OnFocusChangeListener() {
		          @Override
		          public void onFocusChange(View view, boolean hasFocus) {
		        	  if(!hasFocus)
		        	  {
		        		  Calculate(Fields.TaxRate);
		        	  }
		          }
		    });
	      
	      m_TipAmountText.setOnFocusChangeListener(new OnFocusChangeListener() {
		          @Override
		          public void onFocusChange(View view, boolean hasFocus) {
		        	  if(!hasFocus)
		        	  {
		        		  Calculate(Fields.TipAmount);	
		        	  }
		          }
		    });
    }
    
    public void Calculate(Fields typeOfField)
    {
    	if(ValidateFields(typeOfField))
    	{
    		float actualBillSubtotal = 0f;
    		float actualBillTotal = 0f;
    		float tipTotal = 0f;
    		
    		m_Data.m_FoodCosts.clear();
    		
    		for(int i = 0; i < m_PersonLayouts.size(); ++i) // Sum up bill subtotal
    		{
    			float tCost =  m_PersonLayouts.get(i).m_FoodCost;
    			actualBillSubtotal += tCost;
    			m_Data.m_FoodCosts.add(tCost);
    		}
    		
    		//Add Tax to bill subtotal
    		actualBillTotal = actualBillSubtotal + (actualBillSubtotal * m_Data.m_TaxRate);
    		
    		//Calculate Tip
    		tipTotal = actualBillTotal * m_Data.m_TipAmount;
    		
    		//Apply Groupon to bill before tip
    		actualBillTotal -= m_Data.m_GrouponValue;
    		actualBillTotal = Math.max(actualBillTotal, 0f);
    		
    		//Add tip
    		actualBillTotal += tipTotal;
    		
    		for(int i = 0; i < m_PersonLayouts.size(); ++i) // Determine each person's bill share and refresh fields
    		{
    			m_PersonLayouts.get(i).m_BillShare = m_PersonLayouts.get(i).m_FoodCost / actualBillSubtotal;
    			m_PersonLayouts.get(i).RecalculateFields(actualBillTotal);
    		}	
    	}
    }
    
    private boolean ValidateAllFields()
    {
    	boolean retBool = true;
    	
    	retBool = retBool ? ValidateFields(Fields.GrouponCost) : retBool;
    	retBool = retBool ? ValidateFields(Fields.GrouponValue) : retBool;
    	retBool = retBool ? ValidateFields(Fields.SizeOfGroup) : retBool;
    	retBool = retBool ? ValidateFields(Fields.TaxRate) : retBool;
    	retBool = retBool ? ValidateFields(Fields.TipAmount) : retBool;
    	
    	for(int i = 0; i < m_PersonLayouts.size(); ++i)
    	{
    		retBool = retBool ? m_PersonLayouts.get(i).ValidateFields(false) : retBool;
    	}
    	
    	return retBool;
    }
    
    private boolean ValidateFields(Fields typeOfField)
    {
    	switch(typeOfField)
    	{
    		case All:
    			return ValidateAllFields();
	    	case GrouponCost:
				String tempStr1 = m_GrouponCostText.getText().toString();
				float tempVal1 = StaticObjects.ValidatePositiveFloat(
						StaticObjects.StripSpecialNumberCharacters(tempStr1), 
						m_GrouponCostLabel.getText().toString(), this);
				
				if(tempVal1 < 0)
				{
					return false;
				}
				
				m_Data.m_GrouponCost = tempVal1;
	    		m_GrouponCostText.setText(StaticObjects.FormatCurrency(Float.toString(m_Data.m_GrouponCost)));
		    	break;
    	
	    	case GrouponValue:
				String tempStr2 = m_GrouponValueText.getText().toString();
				float tempVal2 = StaticObjects.ValidatePositiveFloat(
						StaticObjects.StripSpecialNumberCharacters(tempStr2), 
						m_GrouponValueLabel.getText().toString(), this);
				
				if(tempVal2 < 0)
				{
					return false;
				}
				
				m_Data.m_GrouponValue = tempVal2;
	    		m_GrouponValueText.setText(StaticObjects.FormatCurrency(Float.toString(m_Data.m_GrouponValue)));
		    	break;
		    	
	    	case SizeOfGroup:
	    		String tempStr3 = m_SizeOfGroupText.getText().toString();
	    		int tempVal3 = StaticObjects.ValidatePositiveInt(tempStr3, 
	    				m_SizeOfGroupLabel.getText().toString(), this);
	    		
	    		if(tempVal3 < 0)
	    		{
	    			return false;
	    		}
	    		
	    		m_Data.m_SizeOfGroup = tempVal3;
		    	break;
		    	
	    	case TaxRate:
				String tempStr4 = m_TaxRateText.getText().toString();
				float tempVal4 = StaticObjects.ValidatePositiveFloat(
						StaticObjects.StripSpecialNumberCharacters(tempStr4), 
						m_TaxRateLabel.getText().toString(), this);
				
				if(tempVal4 < 0)
				{
					return false;
				}
				
				m_Data.m_TaxRate = tempVal4;
	    		m_TaxRateText.setText(StaticObjects.FormatPercent(Float.toString(m_Data.m_TaxRate)));
	    		m_Data.m_TaxRate /= 100f;
		    	break;
		    	
	    	case TipAmount:	
				String tempStr5 = m_TipAmountText.getText().toString();
				float tempVal5 = StaticObjects.ValidatePositiveFloat(
						StaticObjects.StripSpecialNumberCharacters(tempStr5), 
						m_TipAmountLabel.getText().toString(), this);
				
				if(tempVal5 < 0)
				{
					return false;
				}
				
				m_Data.m_TipAmount = tempVal5;
	    		m_TipAmountText.setText(StaticObjects.FormatPercent(Float.toString(m_Data.m_TipAmount)));
	    		m_Data.m_TipAmount /= 100f;
		    	break;
    	}
    	
    	return true;
    }
    
    private void RefreshPersonLayouts(boolean readFoodCosts)
    {
    	int numPersons = 0;
    	
    	// Determine the correct number of persons
    	if(readFoodCosts)
    	{
    		numPersons = m_Data.m_FoodCosts.size();
    	}
    	else
    	{
			numPersons = StaticObjects.ValidatePositiveInt(m_SizeOfGroupText.getText().toString(),
							m_SizeOfGroupLabel.getText().toString(), this);
			
			if(numPersons < 0)
			{
				return;
			}
			
    	}
    	
    	// Update the person layouts to match the number of persons
    	if(numPersons <= m_PersonLayouts.size())
    	{
    		while(m_PersonLayouts.size() > numPersons)
    		{
    			// Remove the last person view
    			m_MainLinearLayout.removeView(m_PersonLayouts.get(m_PersonLayouts.size() - 1).m_PersonLinearLayout);
    			m_PersonLayouts.remove(m_PersonLayouts.size() - 1);
    		}
    	}
    	else
    	{
    		while(m_PersonLayouts.size() < numPersons)
    		{
    			// Inflate the layout from the xml resource
    			LayoutInflater inflater = this.getLayoutInflater();
    			LinearLayout newPersonLayout = (LinearLayout)inflater.inflate(R.layout.person, null);
    			m_MainLinearLayout.addView(newPersonLayout, 6 + m_PersonLayouts.size());
    			
    			//Set the parameters
    			LinearLayout.LayoutParams newParams = (LinearLayout.LayoutParams)newPersonLayout.getLayoutParams();
    			newParams.setMargins(4, 4, 4, 4);
    			newParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
    			newParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
    			newPersonLayout.setLayoutParams(newParams);
    			
    			Person newPerson = new Person(this, newPersonLayout, m_PersonLayouts.size(), readFoodCosts);
    			m_PersonLayouts.add(newPerson);
    		}
    	}
    }
    
    private void RefreshMainFields()
    {
    	m_GrouponCostText.setText(StaticObjects.FormatCurrency(Float.toString(m_Data.m_GrouponCost)));
    	m_GrouponValueText.setText(StaticObjects.FormatCurrency(Float.toString(m_Data.m_GrouponValue)));
    	m_SizeOfGroupText.setText(Integer.toString(m_Data.m_SizeOfGroup));
    	m_TaxRateText.setText(StaticObjects.FormatPercent(Float.toString(m_Data.m_TaxRate * 100f)));
    	m_TipAmountText.setText(StaticObjects.FormatPercent(Float.toString(m_Data.m_TipAmount * 100f)));
    }
    
    private void LoadSystemMessages()
    {
    	URLCall myCall = new URLCall(this);
    	URL url = myCall.FormatURL(StaticObjects.BaseURL() + "GetGrouponSystemMessages.php");
    	myCall.execute(url);
    }	
    
    private void UpdateSystemMessageFromCache()
    {
    	String cachedMessage = StaticObjects.GetPreferenceString(this, SYSTEM_MESSAGE_CACHED_KEY, "");
    	m_DisclaimerText.setText(cachedMessage);
    }
    
    private boolean ShouldCallForSystemMessage()
    {
    	float lastTimestamp = StaticObjects.GetPreferenceFloat(this, SYSTEM_MESSAGE_TIMESTAMP_KEY, 0f);
    	
    	if(lastTimestamp == 0f) // We never downloaded from the server before
    	{
    		StaticObjects.SetPreferenceString(this, SYSTEM_MESSAGE_CACHED_KEY, m_Res.getText(R.string.disclaimer).toString());
    	}

    	if(StaticObjects.CheckIsOnline(this))
    	{
    		float curTime = Calendar.getInstance().getTimeInMillis();
    		
    		if(lastTimestamp == 0f) // Never downloaded before
    		{
    			return true;
    		}
    		else if(curTime - lastTimestamp >= 12f * 3600000f) // It has been 12 hours since the last download
    		{
    			return true;
    		}
    		else // It has not been 12 hours yet since the last download
    		{
    			return false;
    		}
    	}
    	else // No internet
    	{
    		return false;
    	}
    }
    
    
    // URLCaller Methods //
    public void SuccessCallback(String result)
    {	
    	// Cache result and set timestamp of last successful download
    	StaticObjects.SetPreferenceString(this, SYSTEM_MESSAGE_CACHED_KEY, result);
    	StaticObjects.SetPreferenceFloat(this, SYSTEM_MESSAGE_TIMESTAMP_KEY, Calendar.getInstance().getTimeInMillis());
    	
    	UpdateSystemMessageFromCache();
    }
    
    public void ErrorCallback(String result)
    {
    	// If there was any problem retrieving data we will simply fail silently.
    }
    
    public Activity GetActivity()
    {
    	return this;
    }
}