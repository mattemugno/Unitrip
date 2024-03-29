package communication;

import com.ericsson.otp.erlang.*;
import dto.Trip;
import dto.User;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;

//i metodi di questa classe sono chiamati dai vari servlet
//in questo modulo si devono inoltratre i messaggi di richiesta dei vari client al server di erlang
public class MessageHandler{
    //private static final String serverNode = "server@localhost";
    private static final String serverNode = "server@172.18.0.26";
    private static final String serverPID = "loop_server";
    private static int id = 0;

    public String register_message(HttpSession s, User user) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangTuple otpErlangTuple = new OtpErlangTuple(new OtpErlangObject[]{new OtpErlangString(user.getUsername()),
                new OtpErlangString(user.getPassword())});
        send(s, serverPID, new OtpErlangAtom("register"), otpErlangTuple);
        return receiveResponse(s);
    }

    public String login_message(HttpSession s, User user) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangTuple otpErlangTuple = new OtpErlangTuple(new OtpErlangObject[]{new OtpErlangString(user.getUsername()),
                new OtpErlangString(user.getPassword())});
        send(s, serverPID, new OtpErlangAtom("login"), otpErlangTuple);
        return receiveResponse(s);
    }

    public String create_trip(HttpSession s, String trip_name, String destination, long date, String founder, int seats) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangTuple otpErlangTuple = new OtpErlangTuple(new OtpErlangObject[]{new OtpErlangString(trip_name),
                new OtpErlangString(founder), new OtpErlangString(destination),
                new OtpErlangLong(date), new OtpErlangInt(seats)});
        send(s, serverPID, new OtpErlangAtom("create_trip"), otpErlangTuple);
        return receiveResponseTripCreation(s);
    }

    public String add_participant(HttpSession s, String user, String trip_name) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangPid target_pid = get_trip_pid(s, trip_name);
        //System.out.println("PID RICEVUTO: " + target_pid);
        sendToPid(s, target_pid, new OtpErlangAtom("new_partecipant"), new OtpErlangString(user));
        return receiveResponseAddPart(s);
    }

    public String add_favorite(HttpSession s, String user, String trip_name) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangPid target_pid = get_trip_pid(s, trip_name);
        //System.out.println("PID RICEVUTO: " + target_pid);
        sendToPid(s, target_pid, new OtpErlangAtom("add_to_favorites"), new OtpErlangString(user));
        return receiveResponseFavs(s);
    }

    private OtpErlangPid get_trip_pid(HttpSession s, String trip_name) throws OtpErlangDecodeException, OtpErlangExit {
        sendGetTripByName(s, serverPID, new OtpErlangAtom("get_trip_by_name"), new OtpErlangString(trip_name));
        return receivePid(s);
    }

    private Trip get_trip_info(HttpSession s, String trip_name) throws OtpErlangDecodeException, OtpErlangExit, OtpErlangRangeException, ParseException {
        sendGetTripByName(s, serverPID, new OtpErlangAtom("get_trip_by_name"), new OtpErlangString(trip_name.replace("\"","")));
        return receiveTrip(s);
    }

    public String remove_participant(HttpSession s, String user, String trip_name) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangPid target_pid = get_trip_pid(s, trip_name);
        System.out.println("PID RICEVUTO: " + target_pid);
        sendToPid(s, target_pid, new OtpErlangAtom("delete_partecipant"), new OtpErlangString(user));
        return receiveResponse(s);
    }

    public ArrayList<Trip> get_active_trips(HttpSession s) throws OtpErlangDecodeException, OtpErlangExit, OtpErlangRangeException, ParseException {
        send(s, serverPID, new OtpErlangAtom("get_trips"));
        return receiveList(s);
    }

    public String delete_trip(HttpSession s, String trip) throws OtpErlangDecodeException, OtpErlangExit {
        sendDeleteTrip(s, serverPID, new OtpErlangAtom("delete_trip"), new OtpErlangString(trip));
        return receiveResponse(s);
    }

    public String delete_favorite(HttpSession s, String user, String trip_name) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangPid target_pid = get_trip_pid(s, trip_name);
        System.out.println("PID RICEVUTO: " + target_pid);
        sendToPid(s, target_pid, new OtpErlangAtom("delete_from_favorites"), new OtpErlangString(user));
        return receiveResponseFavs(s);
    }

    public ArrayList<Trip> get_favourite_trips(HttpSession s, String user) throws OtpErlangException, ParseException {
        ArrayList<Trip> trip_favorites = new ArrayList<>();
        OtpErlangList app = get_favourite_list(s, user); //mi ritorna una stringList dei nomi dei viaggi preferiti di user
        OtpErlangList favourites = (OtpErlangList) app.elementAt(0);
        //System.out.println("LISTA FINALE: " + favourites);
        if (favourites == null)
            return null;
        for(OtpErlangObject trip_name : favourites){
            //System.out.println("TRIP NAME INSIDE LIST: " + trip_name.toString());
            Trip trip = get_trip_info(s, trip_name.toString()); //per ogni elem di questa lista vado a cercarmi il trip relativo
            trip_favorites.add(trip);
        }
        return trip_favorites;
    }

    private OtpErlangList get_favourite_list(HttpSession s, String user) throws OtpErlangDecodeException, OtpErlangExit {
        sendFavorites(s, serverPID, new OtpErlangAtom("get_user_favorites"), new OtpErlangString(user));
        return receiveResponseListFavorites(s);
    }

    private void sendDeleteTrip(HttpSession session, String serverPID, OtpErlangAtom otpErlangAtom, OtpErlangString otpErlangString) {
        OtpMbox otpMbox = OtpMboxGeneration.getInstance(session); //creo la mailbox a cui mi risponderà il server
        System.out.println("funzione: " + otpErlangAtom + " ,Mbox creata");
        //System.out.println(otpMbox.self());
        OtpErlangTuple request = new OtpErlangTuple(new OtpErlangObject[]{otpMbox.self(), otpErlangAtom, otpErlangString});
        System.out.println("Request Message: " + request);
        otpMbox.send(serverPID, serverNode, request);
    }

    public void send(HttpSession session, String serverPID, OtpErlangAtom otpErlangAtom) {
        OtpMbox otpMbox = OtpMboxGeneration.getInstance(session); //creo la mailbox a cui mi risponderà il server
        System.out.println("funzione: " + otpErlangAtom + " ,Mbox creata");
        //System.out.println(otpMbox.self());
        OtpErlangTuple request = new OtpErlangTuple(new OtpErlangObject[]{otpMbox.self(), otpErlangAtom});
        System.out.println("Request Message: " + request);
        otpMbox.send(serverPID, serverNode, request);
    }

    public void sendGetTripByName(HttpSession session, String serverPID, OtpErlangAtom otpErlangAtomOne, OtpErlangString name) {
        OtpMbox otpMbox = OtpMboxGeneration.getInstance(session);
        System.out.println("funzione: " + otpErlangAtomOne + " ,Mbox creata");
        OtpErlangTuple request = new OtpErlangTuple(new OtpErlangObject[]{otpMbox.self(), otpErlangAtomOne, name });
        //System.out.println("REQUEST(sendToGetPid): " + request);
        System.out.println("Request Message: " + request);
        otpMbox.send(serverPID, serverNode, request);
    }

    public void send(HttpSession session, String serverPID, OtpErlangAtom otpErlangAtom, OtpErlangTuple otpErlangTuple){
        OtpMbox otpMbox = OtpMboxGeneration.getInstance(session); //creo la mailbox a cui mi risponderà il server
        System.out.println("funzione: " + otpErlangAtom + " ,Mbox creata");
        //System.out.println("tupla: " + otpErlangTuple);
        OtpErlangTuple request = new OtpErlangTuple(new OtpErlangObject[]{otpMbox.self(), otpErlangAtom, otpErlangTuple});
        System.out.println("Request Message: " + request);
        otpMbox.send(serverPID, serverNode, request);
        //System.out.println("Send Mbox fatta");
    }

    public void sendToPid(HttpSession session, OtpErlangPid trip_process, OtpErlangAtom otpErlangAtom, OtpErlangString otpErlangTuple){
        OtpMbox otpMbox = OtpMboxGeneration.getInstance(session); //creo la mailbox a cui mi risponderà il server
        System.out.println("funzione: " + otpErlangAtom + " ,Mbox creata");
        //System.out.println("sendToPId: Mbox creata");
        OtpErlangTuple request = new OtpErlangTuple(new OtpErlangObject[]{otpMbox.self(), otpErlangAtom, otpErlangTuple});
        //System.out.println("REQUEST ADD FAVORITES/PARTICPANT: " + request);
        System.out.println("Request Message: " + request);
        otpMbox.send(trip_process, request);
        //System.out.println("sendToPid: Inviata");
    }

    private void sendFavorites(HttpSession session, String serverPID, OtpErlangAtom otpErlangAtom, OtpErlangString otpErlangString) {
        OtpMbox otpMbox = OtpMboxGeneration.getInstance(session); //creo la mailbox a cui mi risponderà il server
        System.out.println("funzione: " + otpErlangAtom + " ,Mbox creata");
        //System.out.println(otpMbox.self());
        OtpErlangTuple request = new OtpErlangTuple(new OtpErlangObject[]{otpMbox.self(), otpErlangAtom, otpErlangString});
        System.out.println("Request Message: " + request);
        otpMbox.send(serverPID, serverNode, request);
    }

    public String receiveResponse(HttpSession session) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangAtom status = new OtpErlangAtom("");
        OtpMbox otpMbox = OtpMboxGeneration.getInstance(session);
        //System.out.println("MBOX CREATA ASPETTO RISPOSTA");
        OtpErlangObject message = otpMbox.receive();
        System.out.println("Response Message: " + message);
        if(message instanceof OtpErlangTuple){
            OtpErlangTuple responseTuple = (OtpErlangTuple) ((OtpErlangTuple) message).elementAt(1);
            status = (OtpErlangAtom) (responseTuple).elementAt(1); //vado a vedere solo l'esito della mia richiesta
        }
        System.out.println(status.toString()); //ricevo {atomic,ok} perchè?
        return status.toString();
    }

    public String receiveResponseAddPart(HttpSession session) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangAtom status = new OtpErlangAtom("");
        OtpMbox otpMbox = OtpMboxGeneration.getInstance(session);
        //System.out.println("MBOX CREATA ASPETTO RISPOSTA");
        OtpErlangObject message = otpMbox.receive();
        System.out.println("Response Message: " + message);
        if(message instanceof OtpErlangTuple){
            OtpErlangTuple responseTuple = (OtpErlangTuple) ((OtpErlangTuple) message).elementAt(1);
            status = (OtpErlangAtom) (responseTuple).elementAt(1);
        }
        System.out.println(status.toString()); //ricevo {atomic,ok} perchè?
        return status.toString();
    }

    public String receiveResponseFavs(HttpSession session) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangTuple status_1 = null;
        OtpErlangTuple status_2 = null;
        OtpErlangAtom response_1 = null;
        OtpErlangAtom response_2 = null;
        OtpMbox otpMbox = OtpMboxGeneration.getInstance(session);
        //System.out.println("MBOX CREATA ASPETTO RISPOSTA");
        OtpErlangObject message = otpMbox.receive();
        System.out.println("Response Message: " + message);
        if(message instanceof OtpErlangTuple){
            OtpErlangTuple responseTuple = (OtpErlangTuple) ((OtpErlangTuple) message).elementAt(1);
            status_1 = (OtpErlangTuple) (responseTuple).elementAt(0);
            response_1 = (OtpErlangAtom) status_1.elementAt(1);
            status_2 = (OtpErlangTuple) (responseTuple).elementAt(1);
            response_2 = (OtpErlangAtom) status_2.elementAt(1);
        }
        if((Objects.equals(response_1.toString(), "ok")) && (Objects.equals(response_2.toString(), "ok")))
            return response_1.toString();
        else
            return null;
    }

    public OtpErlangPid receivePid(HttpSession session) throws OtpErlangDecodeException, OtpErlangExit {
        OtpMbox otpMbox = OtpMboxGeneration.getInstance(session);
        //System.out.println("MBOX CREATA ASPETTO RISPOSTA COL PID");
        OtpErlangObject message = otpMbox.receive();
        System.out.println("Response Message: " + message);
        OtpErlangList obj = null;
        OtpErlangList list = null;
        if(message instanceof OtpErlangTuple){
            OtpErlangTuple responseTuple = (OtpErlangTuple) ((OtpErlangTuple) message).elementAt(1);
            System.out.println("response tuple " + responseTuple);
            obj = (OtpErlangList) responseTuple.elementAt(1);
            list = (OtpErlangList) obj.elementAt(0);
        }
        //System.out.println("PID: " + list.elementAt(1));
        return (OtpErlangPid) list.elementAt(1);
    }

    public Trip receiveTrip(HttpSession session) throws OtpErlangDecodeException, OtpErlangExit, OtpErlangRangeException, ParseException {
        OtpMbox otpMbox = OtpMboxGeneration.getInstance(session);
        //System.out.println("MBOX CREATA ASPETTO RISPOSTA COL PID");
        OtpErlangObject message = otpMbox.receive();
        System.out.println("Response Message: " + message);
        OtpErlangList obj = null;
        OtpErlangList list = null;
        Trip trip = null;
        if(message instanceof OtpErlangTuple){
            OtpErlangTuple responseTuple = (OtpErlangTuple) ((OtpErlangTuple) message).elementAt(1);
            //System.out.println("response tuple " + responseTuple);
            obj = (OtpErlangList) responseTuple.elementAt(1);
            list = (OtpErlangList) obj.elementAt(0); //qui ci sono tutte le info del trip
            //System.out.println("LISTA: " + list);
            trip = Trip.parseErlang(list);
        }
        return trip;
    }

    public String receiveResponseTripCreation(HttpSession session) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangAtom status = new OtpErlangAtom("");
        OtpMbox otpMbox = OtpMboxGeneration.getInstance(session);
        //System.out.println("MBOX CREATA ASPETTO RISPOSTA");
        OtpErlangObject message = otpMbox.receive();
        System.out.println("Response Message: " + message);
        if(message instanceof OtpErlangTuple){
            OtpErlangTuple responseTuple = (OtpErlangTuple) ((OtpErlangTuple) message).elementAt(1);
            OtpErlangTuple first = (OtpErlangTuple) responseTuple.elementAt(0);
            status = (OtpErlangAtom) (first).elementAt(1);//vado a vedere solo l'esito della mia richiesta
        }
        //System.out.println(status.toString()); //ricevo {atomic,ok} perchè?
        return status.toString();
    }

    public ArrayList<Trip> receiveList(HttpSession session) throws OtpErlangDecodeException, OtpErlangExit, OtpErlangRangeException, ParseException {
        OtpMbox otpMbox = OtpMboxGeneration.getInstance(session);
        OtpErlangObject message = otpMbox.receive();
        System.out.println("Response Message: " + message);
        ArrayList<Trip> tripList = new ArrayList<>();
        if(message instanceof OtpErlangTuple) {
            OtpErlangList responseList = (OtpErlangList) ((OtpErlangTuple) message).elementAt(1);
            //OtpErlangTuple tuple = new OtpErlangTuple(responseObj[]);
            for(OtpErlangObject obj : responseList){
                OtpErlangTuple tuple = (OtpErlangTuple) obj;
                OtpErlangList event_trip = (OtpErlangList) tuple.elementAt(1);
                for(OtpErlangObject elem: event_trip) {
                    //System.out.println("LISTA: " + elem);
                    Trip trip = Trip.parseErlang((OtpErlangList) elem);
                    tripList.add(trip);
                }
            }
        }
        return tripList;

    }

    private OtpErlangList receiveResponseListFavorites(HttpSession session) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangList favorites = null;
        OtpMbox otpMbox = OtpMboxGeneration.getInstance(session);
        //System.out.println("MBOX CREATA ASPETTO RISPOSTA");
        OtpErlangObject message = otpMbox.receive();
        System.out.println("Response Message: " + message);
        if(message instanceof OtpErlangTuple){
            OtpErlangTuple responseTuple = (OtpErlangTuple) ((OtpErlangTuple) message).elementAt(1);
            favorites = (OtpErlangList) (responseTuple).elementAt(1); //vado a vedere solo l'esito della mia richiesta
            //System.out.println("LIST FAVS: " + favorites);
        }
        return favorites;
    }

}
