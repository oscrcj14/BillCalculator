package com.rabbidgames.groupon;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Person
{
	Groupon m_Owner;
	
	//UI members
	LinearLayout m_PersonLinearLayout;
	TextView m_TitleLabel;
	EditText m_FoodCostText;
	TextView m_FoodCostLabel;
	TextView m_FoodOwedLabel;
	TextView m_GrouponOwedLabel;
	TextView m_TotalOwedLabel;
	
	//Data
	float m_FoodCost;
	float m_BillShare;
	
	public Person(Groupon owner, LinearLayout myRoot, int index, boolean resurrect)
	{
		m_PersonLinearLayout = myRoot;
		m_Owner = owner;
		
		PopulateIDs();
		SetupListeners();
		
		String personStr = m_Owner.m_Res.getText(R.string.person).toString();
		m_TitleLabel.setText(personStr + " " + Integer.toString(index + 1));
		
		m_FoodCostText.setId(index);
		
		ValidateFields(resurrect);
	}
	
	private void PopulateIDs()
	{
		m_FoodCostText = (EditText)m_PersonLinearLayout.findViewById(R.id.PersonFoodCostText);
		m_FoodCostLabel = (TextView)m_PersonLinearLayout.findViewById(R.id.PersonFoodCostLabel);
		m_FoodOwedLabel = (TextView)m_PersonLinearLayout.findViewById(R.id.FoodAmountOwedResult);
		m_GrouponOwedLabel = (TextView)m_PersonLinearLayout.findViewById(R.id.GrouponAmountOwedResult);
		m_TotalOwedLabel = (TextView)m_PersonLinearLayout.findViewById(R.id.TotalAmountOwedResult);
		m_TitleLabel = (TextView)m_PersonLinearLayout.findViewById(R.id.PersonTitleLabel);
	}
	
	private void SetupListeners()
	{
		m_FoodCostText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if(!hasFocus)
				{
					if(ValidateFields(false))
					{
						m_Owner.Calculate(Groupon.Fields.None);
					}
				}
			}
		});
	}
	 
	public boolean ValidateFields(boolean resurrect)
	{
		//Food Cost
		String tempStr = "";
		
		if(resurrect)
		{
			tempStr = Float.toString(m_Owner.m_Data.m_FoodCosts.get(m_Owner.m_PersonLayouts.size()));
		}
		else
		{
			tempStr = m_FoodCostText.getText().toString();
		}
		
		float tempValue = StaticObjects.ValidatePositiveFloat(StaticObjects.StripSpecialNumberCharacters(tempStr),
									m_FoodCostLabel.getText().toString(), m_Owner);
		
		if(tempValue < 0)
		{
			return false;
		}
		
		m_FoodCost = tempValue;
		m_FoodCostText.setText(StaticObjects.FormatCurrency(Float.toString(m_FoodCost)));
		
		return true;
	}
	 
	public void RecalculateFields(float actualBillTotal)
	{
		float foodAmountOwed = actualBillTotal * m_BillShare;
		float grouponAmountOwed = m_Owner.m_Data.m_GrouponCost * m_BillShare;
		float totalAmountOwed = foodAmountOwed + grouponAmountOwed;
		 
		foodAmountOwed = Math.round(foodAmountOwed * 100f) / 100f;
		grouponAmountOwed = Math.round(grouponAmountOwed * 100f) / 100f;
		totalAmountOwed = Math.round(totalAmountOwed * 100f) / 100f;
		 
		m_FoodOwedLabel.setText(StaticObjects.FormatCurrency(Float.toString(foodAmountOwed)));
		m_GrouponOwedLabel.setText(StaticObjects.FormatCurrency(Float.toString(grouponAmountOwed)));
		m_TotalOwedLabel.setText(StaticObjects.FormatCurrency(Float.toString(totalAmountOwed)));
	}
}
