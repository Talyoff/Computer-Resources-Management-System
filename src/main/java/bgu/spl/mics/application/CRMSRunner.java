package bgu.spl.mics.application;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Vector;

import static bgu.spl.mics.application.objects.Data.Type.*;
import static bgu.spl.mics.application.objects.GPU.Type.*;
import static bgu.spl.mics.application.objects.Student.Degree.MSc;
import static bgu.spl.mics.application.objects.Student.Degree.PhD;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) throws IOException, ParseException {
        // Parse the JSON
        Vector<Student> _students = new Vector<>();
        Vector<GPU> _GPUS = new Vector<>();
        Vector<CPU> _CPUS = new Vector<>();
        Vector<ConferenceInformation> _conferences = new Vector<>();
        int[] tickTimeNdur = parse(args[0], _students, _GPUS, _CPUS, _conferences);
        int _tickTime = tickTimeNdur[0];
        int _duration = tickTimeNdur[1];

        // Initiate Micro-Services of GPU & CPU
        Vector<GPUService> _GPUServices = new Vector<>();
        Vector<CPUService> _CPUServices = new Vector<>();
        Vector<StudentService> _studentServices = new Vector<>();
        Cluster cluster = Cluster.getInstance();
        for (int i = 0; i < _GPUS.size(); i++) {
            cluster.addGPU(_GPUS.get(i));
            _GPUServices.add(new GPUService("gpu" + (i + 1), _GPUS.get(i)));
        }

        for (int i = 0; i < _CPUS.size(); i++) {
            cluster.addCPU(_CPUS.get(i));
            _CPUServices.add(new CPUService("cpu" + (i + 1), _CPUS.get(i)));
        }

        // Initiate ConferenceService
        Vector<ConferenceService> _conferenceServices = new Vector<>();
        for (int i = 0; i < _conferences.size(); i++) {
            _conferenceServices.add(new ConferenceService("conference" + (i + 1), _conferences.get(i)));
        }

        // Initiate StudentService
        for (Student student : _students) {
            _studentServices.add(new StudentService(student));
        }

        MessageBusImpl msgBus = MessageBusImpl.getInstance();

        // Subscribe MicroServices to the correct Events/Broadcasts and run them

        Thread[] threadsGPUs = new Thread[_GPUServices.size()];
        for (int i = 0; i < _GPUServices.size(); i++) {
            int finalI = i;
            threadsGPUs[i] = new Thread(() -> _GPUServices.get(finalI).run());
            threadsGPUs[i].start();
        }

        Thread[] threadsCPUs = new Thread[_CPUServices.size()];
        for (int i = 0; i < _CPUServices.size(); i++) {
            int finalI = i;
            threadsCPUs[i] = new Thread(() -> _CPUServices.get(finalI).run());
            threadsCPUs[i].start();
        }
        Thread[] threadsConferences = new Thread[_conferenceServices.size()];
        for (int i = 0; i < _conferenceServices.size(); i++) {
            int finalI = i;
            threadsConferences[i] = new Thread(() -> _conferenceServices.get(finalI).run());
            threadsConferences[i].start();
        }

        Thread[] threadsStudents = new Thread[_studentServices.size()];
        for (int i = 0; i < _studentServices.size(); i++) {
            int finalI = i;
            threadsStudents[i] = new Thread(() -> _studentServices.get(finalI).run());
            threadsStudents[i].start();
        }

        while (msgBus.getSubscriptionsSize() != (_studentServices.size() * 3 + _GPUServices.size() * 4
                + _CPUServices.size() * 2 + _conferenceServices.size() * 3)){
            // wait until all services are subscribed
        }

        TimeService timeService = new TimeService(_tickTime, _duration);
        Thread threadTimeService = new Thread(timeService);
        threadTimeService.start();

        // Conferences "collects" results and publish them at a specific time

        // Program finish execution and generate output file
        // wait for all thread to finish
        try {
            for (Thread t : threadsConferences) {
                t.join();
            }
            for (Thread t : threadsCPUs) {
                t.join();
            }
            for (Thread t : threadsGPUs) {
                t.join();
            }
            for (Thread t : threadsStudents) {
                t.join();
            }
        }
        catch (InterruptedException exception){
            System.out.println("interrupted while waiting for all threads to finish - couldn't issue output file");
        }
        makeOutputFile(_CPUS,_GPUS,_conferences,_students,1);
    }

    private static int[] parse(String args, Vector<Student> _students, Vector<GPU> _GPUS, Vector<CPU> _CPUS,
                              Vector<ConferenceInformation> _conferences) throws IOException, ParseException {
        JSONParser jsonparser = new JSONParser();
        Object obj = jsonparser.parse(new FileReader(args));
        JSONObject JSONObj = (JSONObject) obj;


        // Parse students list
        JSONArray studentsJSONArray = (JSONArray) JSONObj.get("Students");
        for (int i = 0; i < studentsJSONArray.size(); i++) {
            Vector<Model> models = new Vector<>();
            JSONObject student = (JSONObject) studentsJSONArray.get(i);
            String name = (String) student.get("name");
            String department = (String) student.get("department");
            String status = (String) student.get("status");
            Student.Degree studentStatus = studentStringToEnum(status);
            _students.add(new Student(name, department, studentStatus));
            JSONArray modelsJSONArray = (JSONArray) student.get("models");
            // Parsing models
            for (int j = 0; j < modelsJSONArray.size(); j++) {
                JSONObject model = (JSONObject) modelsJSONArray.get(j);
                String modelName = (String) model.get("name");
                String modelType = (String) model.get("type");
                int modelSize = ((Long)model.get("size")).intValue();
                Data.Type dataType = dataStringToEnum(modelType);
                models.add(new Model(modelName, new Data(dataType, modelSize), _students.get(i)));
            }
            _students.get(i).setModels(models);
        }

        //Parse TickTime & Duration
        int[] tickTimeNduration = {((Long)JSONObj.get("TickTime")).intValue(), ((Long)JSONObj.get("Duration")).intValue()};

        Cluster cluster = Cluster.getInstance();

        // Parsing GPUS
        JSONArray gpusJSONArray = (JSONArray) JSONObj.get("GPUS");
        for (int i = 0; i < gpusJSONArray.size(); i++) {
            String gpuType = (String) gpusJSONArray.get(i);
            _GPUS.add(new GPU(gpuStringToEnum(gpuType), cluster, 0));
        }

        // Parsing CPUS
        JSONArray cpusJSONArray = (JSONArray) JSONObj.get("CPUS");
        for (int i = 0; i < cpusJSONArray.size(); i++) {
            int cpuCores = ((Long) cpusJSONArray.get(i)).intValue();
            _CPUS.add(new CPU(cpuCores, cluster));
        }

        // Parsing Conference

        JSONArray conferenceJSONArray = (JSONArray) JSONObj.get("Conferences");
        for(int i = 0; i < conferenceJSONArray.size(); i++){
            JSONObject conf = (JSONObject) conferenceJSONArray.get(i);
            String name = (String) conf.get("name");
            int date = ((Long) conf.get("date")).intValue();
            _conferences.add(new ConferenceInformation(name, date));
        }
        return tickTimeNduration;
    }

    private static GPU.Type gpuStringToEnum(String gpuType){
        if(gpuType.equals("RTX3090"))
            return RTX3090;
        else if(gpuType.equals("RTX2080"))
            return RTX2080;
            else
                return GTX1080;
    }

    private static Student.Degree studentStringToEnum(String status) {
        if(status.equals("MSc") || status.equals("MsC"))
            return MSc;
        else
            return PhD;
    }

    private static Data.Type dataStringToEnum(String modelType) {
        if(modelType.equals("images") || modelType.equals("Images"))
            return Images;
        else if(modelType.equals("text") || modelType.equals("Text"))
                return Text;
            else
                return Tabular;
    }

    private static void makeOutputFile(Vector<CPU> cpus,Vector <GPU> gpus, Vector <ConferenceInformation> conferenceInformations,
                                       Vector <Student> students, int tickToMilis){
        int gpu_time_used = 0;
        int cpu_time_used = 0;
        int batchesProcessed = 0;
        //computing cpus time used, and batches processed.
        for (int i = 0; i < cpus.size(); i++)
        {
            cpu_time_used += cpus.elementAt(i).getTime_used();
            batchesProcessed += cpus.elementAt(i).getDb_processed();
        }

        //computing gpus time used
        for (int i = 0; i < gpus.size(); i++){
            gpu_time_used += gpus.elementAt(i).getTime_used();
        }

        //convert it into millis (from ticks)
        cpu_time_used = cpu_time_used * tickToMilis;
        gpu_time_used = gpu_time_used * tickToMilis;

        try {
            File fout = new File("output_file.txt");
            FileOutputStream fos = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write("Students:");
            bw.newLine();
            for (int i = 0; i < students.size(); i++){
                bw.write(students.elementAt(i).toString());
                bw.newLine();
            }
            bw.write("Conferences:");
            bw.newLine();
            for (int i = 0; i <conferenceInformations.size() ; i++) {
                bw.write(conferenceInformations.elementAt(i).toString());
                bw.newLine();

            }

            bw.write("cpuTimeUsed: " + cpu_time_used);
            bw.newLine();
            bw.write("gpuTimeUsed: " + gpu_time_used);
            bw.newLine();
            bw.write("batchProcessed: " +batchesProcessed);
            bw.close();
        }
        catch (IOException e) {
            System.out.println("error");
        }
    }
}
