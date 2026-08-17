package com.mycompany.backend.resources;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/resources")
public class ApplicationConfig extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        // Global cross-cutting filters
        classes.add(CorsFilter.class);
        classes.add(TokenAuthFilter.class);
        // Resources
        classes.add(AuthResource.class);
        classes.add(AppointmentResource.class);
        classes.add(PatientResource.class);
        classes.add(DentistResource.class);
        classes.add(TreatmentResource.class);
        classes.add(BillResource.class);
        classes.add(ReportResource.class);
        classes.add(StaffResource.class);
        return classes;
    }
}
