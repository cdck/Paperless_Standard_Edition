package xlk.paperless.standard.view.fragment.agenda;

/**
 * @author xlk
 * @date 2020/3/20
 * @desc
 */
public interface IMeetAgenda {
    void initDefault();

    void setAgendaTv(String text);

    void displayFile(String path);

    void showTimeAgenda();
}
