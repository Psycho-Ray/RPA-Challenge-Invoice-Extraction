package dev.botcity.main;

import dev.botcity.maestro_sdk.BotExecutor;
import dev.botcity.maestro_sdk.BotMaestroSDK;
import dev.botcity.maestro_sdk.exception.BotMaestroException;
import dev.botcity.maestro_sdk.model.AlertType;
import dev.botcity.maestro_sdk.model.AutomationTask;
import dev.botcity.maestro_sdk.runner.BotExecution;
import dev.botcity.maestro_sdk.runner.RunnableAgent;
import dev.botcity.modules.InvoiceExtraction.InvoiceExtraction;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;

public class RPA_Challenge implements RunnableAgent {
	BotExecution execution;
	BotMaestroSDK sdk;

	public static void main(String[] args) {
		BotExecutor.run(new RPA_Challenge(), args);
	}

	@Override
	public void action(BotExecution botExecution) {
		//Integração com o Maestro
		this.execution = botExecution;
		this.sdk = new BotMaestroSDK();
		if (execution != null) sdk.login(execution);

		//Process
		try { new InvoiceExtraction().action(); }
		catch (Exception e) { abort(e); }

		//Finishes this task with success on the Maestro
		finish();
	}

	private void abort(Exception details) {
		try {
			//Finaliza o programa com erro na plataforma
			sdk.finishTask(execution.getTaskId(), AutomationTask.FinishStatus.FAILED);
			sdk.alert(execution.getTaskId(), "RPA Challenge: Error during execution", details.getMessage(), AlertType.ERROR);
			sdk.alert(execution.getTaskId(), "Error Details:", ExceptionUtils.getStackTrace(details), AlertType.INFO);
			System.exit(1);
		} catch (IOException | BotMaestroException | NullPointerException e) {
			//Falha na comunicação com a plataforma
			details.printStackTrace();
			e.printStackTrace();
			System.out.println("WARNING: Failed to return error to the platform");
			System.exit(2);
		}
	}

	private void finish() {
		try {
			//Finaliza o programa com sucesso na plataforma
			sdk.finishTask(execution.getTaskId(), AutomationTask.FinishStatus.SUCCESS);
			System.exit(0);
		} catch (IOException | BotMaestroException | NullPointerException e) {
			//Falha na comunicação com a plataforma
			e.printStackTrace();
			System.out.println("WARNING: Failed to return success to the platform");
			System.exit(3);
		}
	}
}