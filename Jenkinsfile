#!groovy
@Library ('jenkins-workflow@feature/gpipe-4.1') _

spring {
    /*OPT*/ verbosity = 2 // 0 (error/warnings), 1 info, 2 debug
    /*MUST*/ country = 'mx' // 2-digits country id: es, co, pe
    /*MUST*/ group = 'fiduciariomx' // Escenia, netcash...
    // It's used to locate the https://globaldevtools.bbva.com/bitbucket/projects/GPIPE/configs_global/application_name.yml
    /*OPT*/ revision = 'feature/vdc-artifactory' // Country's group settings file branch.
    /*MUST*/ uuaa = 'MFID' // component's id; mainly its uuaa
    build = [
    /*MUST*/ maven_settings: 'file:settings_artifactory', // - key:value tuples
    // - env:id if node has a settings.xml with credentials variable
    // - file:id if node has a settings.xml using Jenkins
    /*MUST*/ maven_args: ' ' // maven arguments. Empty is allowed
    ]
    /*MUST*/ java = 'JDK8' //JDK8
    /*MUST*/ maven = 'Maven3.6.3' //3.0.3
}