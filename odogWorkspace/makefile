BASEPATH = /home/tatu/odog/v1.0/odogWorkspace

DIRS = $(BASEPATH)/odogLibraries/c-lib \
	$(BASEPATH)/CodeGenerators/library \
	$(BASEPATH)/CodeGenerators/Host/DE/library \
	$(BASEPATH)/CodeGenerators/Host/DF/library \
	$(BASEPATH)/CodeGenerators/Multicore/DF/library

linux-64:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making all in $(ME)/$$x ; \
			$(MAKE) all "MACHINE_OPT= -m64";\
			) \
		    fi ; \
		done ; \
	fi


linux-32:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making all in $(ME)/$$x ; \
			$(MAKE) all "MACHINE_OPT= -m32";\
			) \
		    fi ; \
		done ; \
	fi

clean:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo clean in $(ME)/$$x ; \
			$(MAKE) clean ;\
			) \
		    fi ; \
		done ; \
	fi
