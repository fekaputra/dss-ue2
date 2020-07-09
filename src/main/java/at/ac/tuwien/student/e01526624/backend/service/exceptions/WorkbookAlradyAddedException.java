package at.ac.tuwien.student.e01526624.backend.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Workbook already added")
public class WorkbookAlradyAddedException extends RuntimeException {
}
