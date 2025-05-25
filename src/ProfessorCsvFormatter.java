public class ProfessorCsvFormatter implements CsvService.CsvFormatter<Professor> {
    @Override
    public String getHeader() {
        return "matricula,nome,departamento";
    }

    @Override
    public String format(Professor professor) {
        return String.format("%s,\"%s\",\"%s\"",
                professor.getMatricula(),
                professor.getNome().replace("\"", "\"\""),
                professor.getDepartamento().replace("\"", "\"\""));
    }
}