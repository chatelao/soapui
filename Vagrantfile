# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|

  config.vagrant.plugins = ['vagrant-cachier', 'vagrant-vbguest']

  # start --olc
  config.vbguest.auto_update = false
  config.vbguest.auto_reboot = false
  # stop  --olc

  config.vm.box = "obpch/obp-dev"

  config.vm.provider "virtualbox" do |vb|
    vb.name = 'obp-build'
    vb.memory = 4069
    vb.cpus = 3
    vb.customize ["modifyvm", :id, "--cpuexecutioncap", "70"]
    vb.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
    vb.customize ["modifyvm", :id, "--natdnsproxy1", "on"]
  end

  #
  # Build and install the adorsys modul
  #
  config.vm.provision "shell", path: "install/obp_git.sh", privileged: false
  config.vm.provision "shell", path: "install/obp_build.sh", privileged: false

end
