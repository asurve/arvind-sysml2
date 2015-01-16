/**
 * IBM Confidential
 * OCO Source Materials
 * (C) Copyright IBM Corp. 2010, 2015
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.ibm.bi.dml.runtime.instructions.cp;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import com.ibm.bi.dml.runtime.DMLRuntimeException;
import com.ibm.bi.dml.runtime.DMLUnsupportedOperationException;
import com.ibm.bi.dml.runtime.controlprogram.ExecutionContext;
import com.ibm.bi.dml.runtime.controlprogram.caching.MatrixObject;
import com.ibm.bi.dml.runtime.matrix.data.MatrixBlock;
import com.ibm.bi.dml.runtime.matrix.operators.BinaryOperator;
import com.ibm.bi.dml.runtime.matrix.operators.Operator;
import com.ibm.bi.dml.runtime.util.DataConverter;


public class MatrixMatrixBuiltinCPInstruction extends BuiltinBinaryCPInstruction
{
	@SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp. 2010, 2015\n" +
                                             "US Government Users Restricted Rights - Use, duplication  disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";
	
	public MatrixMatrixBuiltinCPInstruction(Operator op, 
											   CPOperand in1, 
											   CPOperand in2, 
											   CPOperand out, 
											   String opcode,
											   String istr){
		super(op, in1, in2, out, 2, opcode, istr);
	}
	
	@Override
	public void processInstruction(ExecutionContext ec) 
		throws DMLRuntimeException, DMLUnsupportedOperationException{
        MatrixBlock matBlock1 = ec.getMatrixInput(input1.get_name());
        MatrixBlock matBlock2 = ec.getMatrixInput(input2.get_name());
        
        String opcode = getOpcode();
        
        if ( opcode.equalsIgnoreCase("solve") ) {
        	executeSolve(ec);
        	return;
        }
		
        /* Default behavior of this instruction */
		String output_name = output.get_name();
		BinaryOperator bop = (BinaryOperator) _optr;
		
		MatrixBlock resultBlock = (MatrixBlock) matBlock1.binaryOperations(bop, matBlock2, new MatrixBlock());
		
		ec.setMatrixOutput(output_name, resultBlock);
		
		resultBlock = matBlock1 = matBlock2 = null;
		ec.releaseMatrixInput(input1.get_name());
		ec.releaseMatrixInput(input2.get_name());
	}
	
	void executeSolve(ExecutionContext ec) 
		throws DMLRuntimeException 
	{
		Array2DRowRealMatrix matrixInput = DataConverter.convertToArray2DRowRealMatrix((MatrixObject)ec.getVariable(input1.get_name()));
		Array2DRowRealMatrix vectorInput = DataConverter.convertToArray2DRowRealMatrix((MatrixObject)ec.getVariable(input2.get_name()));
		
		/*LUDecompositionImpl ludecompose = new LUDecompositionImpl(matrixInput);
		DecompositionSolver lusolver = ludecompose.getSolver();
		RealMatrix solutionMatrix = lusolver.solve(vectorInput);*/
		
		// Setup a solver based on QR Decomposition
		QRDecomposition qrdecompose = new QRDecomposition(matrixInput);
		DecompositionSolver solver = qrdecompose.getSolver();
		// Invoke solve
		RealMatrix solutionMatrix = solver.solve(vectorInput);
		
		MatrixBlock solution = DataConverter.convertToMatrixBlock(solutionMatrix.getData());
		
		ec.setMatrixOutput(output.get_name(), solution);
		ec.releaseMatrixInput(input1.get_name());
		ec.releaseMatrixInput(input2.get_name());
		
		return;
	}
}