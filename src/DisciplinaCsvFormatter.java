public class DisciplinaCsvFormatter implements CsvService.CsvFormatter<Disciplina> {
    @Override
    public String getHeader() {
        return "codigo,nome,cargaHoraria,prerequisitos";
    }

    @Override
    public String format(Disciplina disciplina) {
        return String.format("%s,\"%s\",%d,\"%s\"",
                disciplina.getCodigo(),
                disciplina.getNome().replace("\"", "\"\""),
                disciplina.getCargaHoraria(),
                String.join(";", disciplina.getPrerequisitos()));
    }
}