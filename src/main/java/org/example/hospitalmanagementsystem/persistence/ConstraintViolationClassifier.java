package org.example.hospitalmanagementsystem.persistence;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;


public final class ConstraintViolationClassifier {

    private static final String PG_UNIQUE_VIOLATION = "23505";

    private ConstraintViolationClassifier() {
    }


    public static boolean isUniqueViolationOn(
            DataIntegrityViolationException ex,
            String constraintName
    ) {
        Objects.requireNonNull(ex, "ex must not be null");
        Objects.requireNonNull(constraintName, "constraintName must not be null");

        for (Throwable t = ex; t != null; t = nextCause(t)) {

            // 1) Vendor-neutral path (Hibernate / JPA)
            if (t instanceof ConstraintViolationException cve
                    && cve.getConstraintName() != null
                    && matches(cve.getConstraintName(), constraintName)
                    && isUniqueKind(cve)) {
                return true;
            }

            // 2) PostgreSQL driver path
            if (t instanceof PSQLException pg
                    && ConstraintViolationClassifier.PG_UNIQUE_VIOLATION.equals(pg.getSQLState())
                    && extractConstraint(pg)
                    .map(name -> matches(name, constraintName))
                    .orElse(false)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUniqueKind(ConstraintViolationException cve) {
        // Hibernate reports SQLState via getSQLState(); 23505 = unique violation.
        return PG_UNIQUE_VIOLATION.equals(cve.getSQLState());
    }

    private static Optional<String> extractConstraint(PSQLException ex) {
        ServerErrorMessage serverError = ex.getServerErrorMessage();
        return serverError == null
                ? Optional.empty()
                : Optional.ofNullable(serverError.getConstraint());
    }

    /** Exact, case-insensitive comparison. Never use contains(). */
    private static boolean matches(String actual, String expected) {
        return actual.toLowerCase(Locale.ROOT)
                .equals(expected.toLowerCase(Locale.ROOT));
    }

    /** Guards against self-referencing cause loops. */
    private static Throwable nextCause(Throwable t) {
        Throwable cause = t.getCause();
        return cause == t ? null : cause;
    }
}