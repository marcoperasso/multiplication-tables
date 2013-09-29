package com.multiplicationtables;

import java.io.Serializable;
import java.util.ArrayList;

public class Answer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5658903572029737400L;
	private boolean correct;
	private Integer response;

	public Answer(boolean correct, Integer response) {
		this.correct = correct;
		this.response = response;
	}

	public boolean isCorrect() {
		return correct;
	}

	public Integer getResponse() {
		return response;
	}

}

class AnswerList extends ArrayList<Answer> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 269159548646085465L;
}
