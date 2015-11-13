package com.esoft.core.jsf.validator;

import javax.annotation.Resource;
import javax.faces.component.PartialStateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.DoubleRangeValidator;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.omnifaces.util.Components;
import org.omnifaces.util.Messages;
import org.omnifaces.validator.ValueChangeValidator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.esoft.archer.common.exception.InputRuleMatchingException;
import com.esoft.archer.common.exception.NoMatchingObjectsException;
import com.esoft.archer.common.service.ValidationService;
import com.esoft.core.annotations.ScopeType;
import com.esoft.core.util.SpringBeanUtil;

//@Component
//@Scope(ScopeType.REQUEST)
@FacesValidator(value = "com.esoft.core.validator.InputValidator")
public class InputValidator extends ValueChangeValidator implements
		PartialStateHolder {

	public static final String VALIDATOR_ID = "com.esoft.core.validator.InputValidator";

	// 规则编号
	private String ruleId;

	// 验证消息
	private String message;

	@Resource
	ValidationService vdtService;
	
	// 实体
	private String entityClass;
	
	// 字段名称
	private String fieldName;

	@Override
	public void validateChangedObject(FacesContext context,
			UIComponent component, Object arg2) throws ValidatorException {
		String value = (String) arg2;
		ruleId=ruleId.replace("referrer", "username");
		try {
			ValidationService vdtService = (ValidationService) SpringBeanUtil.getBeanByName("validationService");
			vdtService.notEqualsPersistenceValue(entityClass,fieldName,ruleId, value,component);//String entityClass, String fieldName,String id, String value
		
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void restoreState(FacesContext context, Object state) {
		if (context == null) {
			throw new NullPointerException();
		}
		if (state != null) {
			Object values[] = (Object[]) state;
			ruleId = (String) values[0];
			message = (String) values[1];
		}
	}

	@Override
	public Object saveState(FacesContext context) {
		if (context == null) {
			throw new NullPointerException();
		}
		if (!initialStateMarked()) {
			Object values[] = new Object[2];
			values[0] = ruleId;
			values[1] = message;
			return (values);
		}
		return null;
	}

	private boolean transientValue = false;


    public boolean isTransient() {

        return (this.transientValue);

    }


    public void setTransient(boolean transientValue) {

        this.transientValue = transientValue;

    }

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		clearInitialState();
		this.ruleId = ruleId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		clearInitialState();
		this.message = message;
	}

	private boolean initialState;

	public void markInitialState() {
		initialState = true;
	}

	public boolean initialStateMarked() {
		return initialState;
	}

	public void clearInitialState() {
		initialState = false;
	}
	
	
	
    public String getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public boolean equals(Object otherObj) {

        if (!(otherObj instanceof InputValidator)) {
            return false;
        }
        InputValidator other = (InputValidator) otherObj;
        return StringUtils.equals(this.getRuleId(),other.getRuleId())
                && StringUtils.equals(this.getMessage(),other.getMessage());

    }


    public int hashCode() {

        if (StringUtils.isEmpty(this.getMessage()) && StringUtils.isEmpty(this.getRuleId())) {
			return super.hashCode();
		}
        int hashCode = 0;
        if (StringUtils.isNotEmpty(this.getMessage())) {
			hashCode += this.getMessage().hashCode();
		}
        if (StringUtils.isNotEmpty(this.getRuleId())) {
			hashCode += this.getRuleId().hashCode();
		}
        return (hashCode);

    }


}
