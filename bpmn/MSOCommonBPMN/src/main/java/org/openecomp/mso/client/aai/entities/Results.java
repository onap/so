package org.openecomp.mso.client.aai.entities;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "results"
})
@XmlRootElement(name = "results")
public class Results<T> {
	
	@XmlElement(name="results")
	protected List<T> result;
	
    public List<T> getResult() {
        if (result == null) {
        	result = new ArrayList<T>();
        }
        return this.result;
    }
    
    public void setResult(List<T> result) {        
        this.result=result;
    }	
}
